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

    @Value("${hemis.webhook.retry.batch-size:50}")
    private int batchSize;

    @Value("${hemis.webhook.retention.success-days:30}")
    private int successRetentionDays;

    @Value("${hemis.webhook.retention.failed-days:90}")
    private int failedRetentionDays;

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

    // @Transactional YO'Q — ataylab: (1) processDueRetries() → doProcess() self-invocation'da
    // proxy-based @Transactional baribir bypass bo'lardi (Golden Rule #9); (2) redispatch() ichida
    // HTTP POST bor — uni bitta uzun DB tx ichida ushlab turmaslik kerak. Har save alohida tx.
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

    /**
     * Bitta due retry'ni qayta yuborish. Envelope payload endi {@code webhook_delivery_log.payload}'da
     * saqlanadi (V015), shuning uchun {@link WebhookDispatcher#redispatch} to'liq replay qiladi
     * (attempt_n + 1, exponential backoff, max_retries'da DLQ). Eski "to'g'ridan DLQ" placeholder
     * olib tashlandi (ADR-0012 retry endi haqiqatan ishlaydi).
     */
    private void retryOne(WebhookDeliveryLog stale) {
        dispatcher.redispatch(stale);
    }

    /**
     * Retention cleanup — status bo'yicha farqlangan (lean log strategiyasi):
     *   SUCCESS → success-days (default 30) — audit qiymati past, tez tozalanadi
     *   FAILED  → failed-days  (default 90) — troubleshooting uchun uzoqroq
     *   DLQ     → o'chirilmaydi — manual admin review uchun saqlanadi
     * To'liq stack/context Sentry'da (sentry_event_id cross-link). Outbox retention bilan bir xil cron.
     */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void cleanupCompletedLogs() {
        LocalDateTime now = LocalDateTime.now();
        int success = deliveryLogRepository.deleteByStatusAndCompletedAtBefore(
                WebhookDeliveryStatus.SUCCESS, now.minusDays(successRetentionDays));
        int failed = deliveryLogRepository.deleteByStatusAndCompletedAtBefore(
                WebhookDeliveryStatus.FAILED, now.minusDays(failedRetentionDays));
        if (success + failed > 0) {
            log.info("Webhook delivery log retention: {} success (>{}d) + {} failed (>{}d) o'chirildi. DLQ saqlandi.",
                    success, successRetentionDays, failed, failedRetentionDays);
        }
    }
}
