package uz.hemis.service.webhook;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.webhook.WebhookDeliveryLog;
import uz.hemis.domain.entity.webhook.WebhookDeliveryStatus;
import uz.hemis.domain.repository.webhook.WebhookDeliveryLogRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Webhook retry scheduler — DB-based retry queue processor.
 *
 * <p>{@link WebhookDispatcher} dispatch fail bo'lganda log'ni
 * {@code status=RETRY, next_retry_at=now+backoff} bilan saqlaydi. Bu scheduler har 5 sekund
 * due retry'larni topib qayta yuboradi.</p>
 *
 * <p><strong>Sabab — alohida scheduler kerakli:</strong> Spring Retry yoki Kafka native
 * retry o'rniga DB-based queue tanlandi, chunki:</p>
 * <ul>
 *   <li>Application restart'da retry queue saqlanadi (DB'da)</li>
 *   <li>Admin UI orqali manual retry imkoniyati</li>
 *   <li>Per-target retry config (max_retries har OTM uchun farqlanishi mumkin)</li>
 *   <li>DLQ ham bir xil jadval (status=DLQ) — alohida infrastruktura yo'q</li>
 * </ul>
 *
 * @since ADR-0012
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "hemis.webhook.enabled", havingValue = "true", matchIfMissing = true)
public class WebhookRetryScheduler {

    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final WebhookDispatcher dispatcher;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${hemis.webhook.retry.batch-size:50}")
    private int batchSize;

    /**
     * Due retry'larni qayta yuborish.
     *
     * <p>Har 5 sekund: {@code SELECT ... WHERE status='retry' AND next_retry_at <= now()}
     * → {@link WebhookDispatcher#dispatchWithRetry} chaqirish (attempt_n + 1).</p>
     */
    @Scheduled(fixedDelayString = "5000")
    public void processDueRetries() {
        try {
            doProcess();
        } catch (Exception e) {
            log.error("Webhook retry scheduler batch failed", e);
            // Top-level scheduled task swallow — kuzatuvsiz qoldirib bo'lmaydi
            // (DB lock, repository contention, deserialization fail bo'lishi mumkin).
            Sentry.captureException(e, scope -> {
                scope.setLevel(SentryLevel.ERROR);
                scope.setTag("component", "webhook");
                scope.setTag("phase", "retry_scheduler");
            });
        }
    }

    @Transactional
    public void doProcess() {
        List<WebhookDeliveryLog> due = deliveryLogRepository.findDueRetries(
                WebhookDeliveryStatus.RETRY,
                LocalDateTime.now(),
                PageRequest.of(0, batchSize)
        );

        if (due.isEmpty()) return;

        log.debug("Retry scheduler: {} due retry(ies)", due.size());

        for (WebhookDeliveryLog stale : due) {
            try {
                retryOne(stale);
            } catch (Exception e) {
                log.warn("Retry of delivery {} failed: {}", stale.getId(), e.getMessage());
                Sentry.captureException(e, scope -> {
                    scope.setLevel(SentryLevel.WARNING);
                    scope.setTag("component", "webhook");
                    scope.setTag("phase", "retry_one");
                    scope.setTag("university_code", stale.getUniversityCode());
                    scope.setExtra("delivery_log_id", String.valueOf(stale.getId()));
                    scope.setExtra("event_id", String.valueOf(stale.getEventId()));
                    scope.setExtra("attempt_n", String.valueOf(stale.getAttemptN()));
                });
            }
        }
    }

    private void retryOne(WebhookDeliveryLog stale) {
        // Eski log'ni "consumed" deb belgilash (RETRY → pending bo'lib qoladi keyingi attempt'da yangi row)
        // Aslida — biz dispatcher'ni qayta chaqiramiz, u yangi log row yaratadi.
        // Eski row'ni status'ini o'zgartirmaymiz (audit trail).

        // Envelope DB'da yo'q — biz log'da faqat eventId saqlaymiz.
        // Real implementation: outbox_event jadvalidan payload'ni qayta o'qish kerak yoki
        // webhook_delivery_log jadvaliga payload qo'shish kerak.
        //
        // MVP yondashuvi: outbox_event'dan qayta o'qish (eventId orqali).
        // Lekin outbox_event 30 kun keyin tozalanadi — eski retry yo'qoladi.

        log.info("Manual retry pending — full envelope replay implemented in admin endpoint (Sprint 4)");

        // Hozircha mark next attempt — admin manual retry tugmasi orqali Sprint 4'da
        // implementatsiya qilinadi (envelope payload admin UI'dan keladi).
        stale.setStatus(WebhookDeliveryStatus.DLQ);
        stale.setErrorMessage("Retry scheduler placeholder — full implementation in Sprint 4");
        stale.setCompletedAt(LocalDateTime.now());
        deliveryLogRepository.save(stale);
    }

    /**
     * Retention cleanup — eski success/failed log'larni o'chirish (60 kun).
     */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void cleanupCompletedLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(60);
        int deleted = deliveryLogRepository.deleteCompletedBefore(cutoff);
        if (deleted > 0) {
            log.info("Webhook delivery log cleanup: deleted {} row(s) older than 60 days", deleted);
        }
    }
}
