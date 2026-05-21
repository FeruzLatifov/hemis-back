package uz.hemis.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.outbox.OutboxEvent;
import uz.hemis.domain.repository.outbox.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Transactional Outbox poller — DB → Kafka background dispatcher.
 *
 * <p>Har {@code hemis.outbox.poller.interval-ms} (default 1s) intervalda outbox jadvalidan
 * {@code published_at IS NULL} row'larni o'qib, har birini tegishli Kafka topic'iga
 * jo'natadi (topic naming: {@code hemis.{aggregateType}.events.v{schemaVersion}}).</p>
 *
 * <p><strong>Multi-instance safety:</strong> {@code FOR UPDATE SKIP LOCKED} — Kubernetes
 * 3+ replica scenarioda har replica o'z partition'ini oladi (lock conflict yo'q).</p>
 *
 * <p><strong>Retry semantics:</strong></p>
 * <ul>
 *   <li>Kafka send fail → {@code retry_count++}, {@code last_error} update</li>
 *   <li>retry_count &gt;= 100 (V014 CHECK) → row "stuck" — DLQ candidate ({@code findDlqCandidates})</li>
 *   <li>Manual admin retry: published_at NULL'ga qaytarish</li>
 * </ul>
 *
 * @since ADR-0007 / ADR-0010
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "hemis.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPoller {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${hemis.outbox.poller.batch-size:100}")
    private int batchSize;

    @Value("${hemis.outbox.retention.days:30}")
    private int retentionDays;

    /** Per-event Kafka send timeout (sekund). */
    private static final long KAFKA_SEND_TIMEOUT_SEC = 10;

    @Scheduled(fixedDelayString = "${hemis.outbox.poller.interval-ms:1000}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void pollAndPublish() {
        try {
            processBatch();
        } catch (Exception e) {
            // Top-level catch — scheduler exception scheduler'ni bloklab qo'ymasligi uchun
            log.error("Outbox poller batch failed", e);
            io.sentry.Sentry.captureException(e, scope -> {
                scope.setLevel(io.sentry.SentryLevel.ERROR);
                scope.setTag("component", "outbox");
                scope.setTag("phase", "poller_batch");
            });
        }
    }

    /**
     * Bir batch event'ni transaction ichida o'qish + Kafka'ga jo'natish.
     *
     * <p>Transaction scope: pollUnpublishedForUpdate + markPublished/markRetry. Lock SKIP_LOCKED
     * bilan boshqa replica'lar o'tkazib yuboradi. Transaction commit Kafka send'dan keyin —
     * Kafka write success bo'lsa published_at o'rnatiladi.</p>
     *
     * <p><strong>Diqqat:</strong> @Transactional annotation pollAndPublish'da — Spring AOP
     * self-invocation trap (this.processBatch() proxy bypass qiladi). Annotation bu yerda
     * informational maqsadli — actual tx pollAndPublish'dan keladi.</p>
     */
    public void processBatch() {
        List<OutboxEvent> events = repository.pollUnpublishedForUpdate(batchSize);
        if (events.isEmpty()) {
            return;
        }

        log.debug("Outbox poller: {} pending event(s)", events.size());

        for (OutboxEvent event : events) {
            try {
                publishOne(event);
                repository.markPublished(event.getId());
            } catch (Exception e) {
                String error = describeError(e);
                log.warn("Failed to publish outbox event {}: {}", event.getId(), error);
                repository.markRetry(event.getId(), error);
            }
        }
    }

    private void publishOne(OutboxEvent event)
            throws InterruptedException, ExecutionException, TimeoutException {

        String topic = event.getKafkaTopic();
        String key = event.getAggregateId();
        String value = event.getPayload();

        SendResult<String, String> result = kafkaTemplate
                .send(topic, key, value)
                .get(KAFKA_SEND_TIMEOUT_SEC, TimeUnit.SECONDS);

        RecordMetadata metadata = result.getRecordMetadata();
        if (log.isDebugEnabled()) {
            log.debug("Published event {} → topic={} partition={} offset={}",
                    event.getId(), metadata.topic(), metadata.partition(), metadata.offset());
        }
    }

    private static String describeError(Exception e) {
        String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
        // Cap to 1000 chars (last_error TEXT but avoid log spam)
        return msg.length() > 1000 ? msg.substring(0, 1000) + "...[truncated]" : msg;
    }

    /**
     * Retention cleanup — eski published event'larni o'chirish (default 30 kun).
     * Har kun ertalab 03:00 da ishlaydi (low-traffic vaqt).
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldPublished() {
        // retentionDays application.yml dan keladi (`hemis.outbox.retention.days`, default 30).
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = repository.deletePublishedBefore(cutoff);
        if (deleted > 0) {
            log.info("Outbox retention cleanup: deleted {} published event(s) older than {} days",
                    deleted, retentionDays);
        }
    }
}
