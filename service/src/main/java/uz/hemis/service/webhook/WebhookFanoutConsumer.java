package uz.hemis.service.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.support.SendResult;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Webhook fanout consumer — domain event'larni 224 OTM bo'yicha tarqatuvchi.
 *
 * <p><strong>Flow:</strong></p>
 * <pre>
 *   hemis.classifier.events.v1  ──┐
 *   hemis.rule.events.v1        ──┼──▶ FanoutConsumer ──▶ hemis.webhook.events
 *   hemis.university.events.v1  ──┘                       (per-OTM message:
 *                                                          key=university_code)
 * </pre>
 *
 * <p>Aktiv {@link WebhookTarget} ro'yxati bo'yicha har domain event uchun N ta
 * webhook message yaratiladi (N = aktiv OTM soni, ~224). WebhookDispatcher consumer
 * keyin bu message'larni parallel REST callback'larga aylantiradi.</p>
 *
 * <p><strong>Topic filter:</strong> Bu fanout faqat <em>Univer'ga tegishli</em> event'lar
 * uchun. Internal-only event'lar (audit, employee_sync) fanout qilinmaydi.</p>
 *
 * @since ADR-0012
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "hemis.webhook.enabled", havingValue = "true", matchIfMissing = true)
public class WebhookFanoutConsumer {

    private final WebhookTargetRepository targetRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /** Fanout send'larni tasdiqlashni kutish limiti (barcha OTM message broker'ga yozilishi). */
    private static final long FANOUT_SEND_TIMEOUT_SEC = 30;

    /** Outbox event.id header nomi — {@code OutboxPoller} wire-contract bilan mos bo'lishi shart. */
    private static final String HEADER_EVENT_ID = "hemis-event-id";

    /**
     * Webhook'ga jo'natiladigan domain topic'lar ro'yxati.
     *
     * <p>Yangi tip qo'shilsa shu yerga qo'shiladi. Kafka {@code @KafkaListener}
     * SpEL'da statik string array kerak — array constant'dan o'qiladi.</p>
     */
    public static final String[] WEBHOOK_ELIGIBLE_TOPICS = {
            "hemis.classifier.events.v1",
            "hemis.rule.events.v1",
            "hemis.university.events.v1"
    };

    @KafkaListener(
            topics = {
                    "hemis.classifier.events.v1",
                    "hemis.rule.events.v1",
                    "hemis.university.events.v1"
            },
            groupId = "hemis-webhook-fanout",
            concurrency = "3"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String topic = record.topic();
        String aggregateId = record.key();
        String payload = record.value();

        try {
            // Aktiv OTM ro'yxati — 2026-05-18 refactor: university.active JOIN orqali
            // (webhook_target.active field olib tashlandi, source of truth = hemishe_e_university)
            List<WebhookTarget> activeTargets = targetRepository.findAllForActiveUniversities();
            if (activeTargets.isEmpty()) {
                log.debug("No active webhook targets — skipping fanout for {}/{}", topic, aggregateId);
                ack.acknowledge();
                return;
            }

            // Deterministik event_id — manba outbox event.id (Kafka header) → redelivery'da
            // Univer idempotency dedup buzilmaydi (random UUID EMAS, SYNC-WH-02).
            String sourceEventId = extractHeader(record, HEADER_EVENT_ID);

            // Envelope yaratish (domain payload'ni Univer-friendly format'ga o'rash)
            String envelope = buildEnvelope(topic, aggregateId, payload, sourceEventId);

            // Har OTM uchun alohida message (key=university_code → per-OTM partition).
            // Barcha send TASDIQLANMAGUNCHA kutamiz, faqat keyin ack. Aks holda ack↔broker-flush
            // oynasida crash bo'lsa buffer'dagi xabarlar yo'qolardi (offset commit bo'lgani uchun
            // qayta yetkazilmasdi — SYNC-WH-03). Bittasi fail bo'lsa → ack YO'Q → Kafka butun
            // event'ni qayta beradi (at-least-once; event_id deterministik → Univer dedup qiladi).
            List<CompletableFuture<SendResult<String, String>>> futures =
                    new ArrayList<>(activeTargets.size());
            for (WebhookTarget target : activeTargets) {
                futures.add(kafkaTemplate.send(
                        "hemis.webhook.events",
                        target.getUniversityCode(),
                        envelope
                ));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(FANOUT_SEND_TIMEOUT_SEC, TimeUnit.SECONDS);

            log.debug("Fanout {}/{} → {} target(s) confirmed", topic, aggregateId, activeTargets.size());
            ack.acknowledge();

        } catch (Exception e) {
            // Top-level catch — Kafka offset commit qilmaslik (retry kelajakda)
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Fanout consumer failed for {}/{}", topic, aggregateId, e);
            io.sentry.Sentry.captureException(e, scope -> {
                scope.setLevel(io.sentry.SentryLevel.ERROR);
                scope.setTag("component", "webhook");
                scope.setTag("phase", "fanout");
                scope.setTag("kafka_topic", topic);
                scope.setExtra("aggregate_id", aggregateId);
            });
            // ack qilmaymiz — Kafka qayta beradi
        }
    }

    /**
     * Domain event payload'ni webhook envelope'ga o'rash.
     *
     * <p>Domain payload — outbox'da {@code OutboxEventPublisher.publish(...)} qiladi.
     * Bu metod o'sha JSON'ni {@link WebhookEventEnvelope} bilan o'raydi.</p>
     */
    private String buildEnvelope(String topic, String aggregateId, String domainPayload, String sourceEventId)
            throws JsonProcessingException {
        // Topic'dan aggregate type ajratish: hemis.classifier.events.v1 → classifier
        String aggregateType = parseAggregateType(topic);
        // hemis-univer kontrakt convention: event_type doim "{aggregate}.updated" formatda.
        // Sub-action (ADD/UPDATE/DELETE) — domain payload ichidagi `data.action` field orqali.
        // Univer ApplyHemisEventJob.applyClassifier() shu pattern bilan ishlaydi.
        String eventType = aggregateType + ".updated";

        // Domain payload'ni Object sifatida parse qilib data field'iga qo'yamiz
        Object data = objectMapper.readValue(domainPayload, Object.class);

        // event_id — deterministik (manba outbox event.id yoki content-hash), random EMAS.
        UUID eventId = deriveEventId(sourceEventId, aggregateType, aggregateId, domainPayload);

        WebhookEventEnvelope envelope = new WebhookEventEnvelope(
                eventId,
                eventType,
                aggregateType,
                aggregateId,
                java.time.LocalDateTime.now(),
                1,  // schema_version
                data
        );

        return objectMapper.writeValueAsString(envelope);
    }

    /**
     * Deterministik {@code event_id}: manba outbox event.id (Kafka header) bo'lsa — o'shani;
     * bo'lmasa (eski/header'siz message) event mazmunidan barqaror UUID hosil qiladi.
     * Hech qachon {@link UUID#randomUUID()} EMAS — redelivery'da bir xil event bir xil id olishi
     * shart, aks holda Univer idempotency dedup buziladi (SYNC-WH-02).
     */
    private static UUID deriveEventId(String sourceEventId, String aggregateType,
                                      String aggregateId, String domainPayload) {
        if (sourceEventId != null && !sourceEventId.isBlank()) {
            try {
                return UUID.fromString(sourceEventId.trim());
            } catch (IllegalArgumentException ignored) {
                // header buzuq — content-hash fallback'ga o'tamiz
            }
        }
        String seed = aggregateType + "|" + aggregateId + "|" + domainPayload;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    /** Kafka record header'idan string qiymat (UTF-8) — yo'q bo'lsa {@code null}. */
    private static String extractHeader(ConsumerRecord<String, String> record, String key) {
        Header header = record.headers().lastHeader(key);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }

    private static String parseAggregateType(String topic) {
        // hemis.classifier.events.v1 → "classifier"
        String[] parts = topic.split("\\.");
        return parts.length >= 2 ? parts[1] : "unknown";
    }
}
