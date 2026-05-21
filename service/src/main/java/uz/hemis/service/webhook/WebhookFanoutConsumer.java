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
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

import java.util.List;
import java.util.UUID;

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

            // Envelope yaratish (domain payload'ni Univer-friendly format'ga o'rash)
            String envelope = buildEnvelope(topic, aggregateId, payload);

            // Har OTM uchun alohida message (key=university_code → per-OTM partition)
            int sent = 0;
            for (WebhookTarget target : activeTargets) {
                try {
                    kafkaTemplate.send(
                            "hemis.webhook.events",
                            target.getUniversityCode(),
                            envelope
                    );
                    sent++;
                } catch (Exception e) {
                    // Bir OTM'ga publish fail bo'lsa qolganlarini bloklamaslik
                    log.warn("Fanout to {} failed: {}", target.getUniversityCode(), e.getMessage());
                }
            }

            log.debug("Fanout {}/{} → {} target(s)", topic, aggregateId, sent);
            ack.acknowledge();

        } catch (Exception e) {
            // Top-level catch — Kafka offset commit qilmaslik (retry kelajakda)
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
    private String buildEnvelope(String topic, String aggregateId, String domainPayload)
            throws JsonProcessingException {
        // Topic'dan aggregate type ajratish: hemis.classifier.events.v1 → classifier
        String aggregateType = parseAggregateType(topic);
        // hemis-univer kontrakt convention: event_type doim "{aggregate}.updated" formatda.
        // Sub-action (ADD/UPDATE/DELETE) — domain payload ichidagi `data.action` field orqali.
        // Univer ApplyHemisEventJob.applyClassifier() shu pattern bilan ishlaydi.
        String eventType = aggregateType + ".updated";

        // Domain payload'ni Object sifatida parse qilib data field'iga qo'yamiz
        Object data = objectMapper.readValue(domainPayload, Object.class);

        WebhookEventEnvelope envelope = new WebhookEventEnvelope(
                UUID.randomUUID(),  // event_id (yangi UUID — Kafka offset'dan ajratilgan)
                eventType,
                aggregateType,
                aggregateId,
                java.time.LocalDateTime.now(),
                1,  // schema_version
                data
        );

        return objectMapper.writeValueAsString(envelope);
    }

    private static String parseAggregateType(String topic) {
        // hemis.classifier.events.v1 → "classifier"
        String[] parts = topic.split("\\.");
        return parts.length >= 2 ? parts[1] : "unknown";
    }
}
