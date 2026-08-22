package uz.hemis.service.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import uz.hemis.domain.entity.webhook.WebhookDeliveryLog;
import uz.hemis.domain.entity.webhook.WebhookDeliveryStatus;
import uz.hemis.domain.repository.webhook.WebhookDeliveryLogRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WebhookRetryScheduler}.
 *
 * <p><strong>Scope:</strong> due-retry polling, per-item exception isolation,
 * {@link WebhookDispatcher#redispatch} ga delegatsiya (haqiqiy replay — 83d98de'dan
 * keyin "to'g'ridan DLQ" placeholder yo'q), retention cleanup cutoff math va
 * {@code @Scheduled} top-level exception swallowing.</p>
 *
 * <p>Pure Mockito — no Spring context, no DB.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookRetryScheduler Tests")
class WebhookRetrySchedulerTest {

    @Mock private WebhookDeliveryLogRepository deliveryLogRepository;
    @Mock private WebhookDispatcher dispatcher;

    @InjectMocks private WebhookRetryScheduler scheduler;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(scheduler, "batchSize", 50);
        ReflectionTestUtils.setField(scheduler, "successRetentionDays", 30);
        ReflectionTestUtils.setField(scheduler, "failedRetentionDays", 90);
    }

    // =========================================================
    // Empty queue
    // =========================================================

    @Test
    @DisplayName("doProcess() with no due retries → early return, no save")
    void doProcess_emptyQueue_returnsEarly() {
        when(deliveryLogRepository.findDueRetries(
                eq(WebhookDeliveryStatus.RETRY), any(LocalDateTime.class), any(PageRequest.class)))
            .thenReturn(List.of());

        scheduler.doProcess();

        verify(deliveryLogRepository, never()).save(any());
    }

    // =========================================================
    // Multiple due retries — all processed
    // =========================================================

    @Test
    @DisplayName("doProcess() with 3 due retries → redispatch() har biri uchun aynan 1 marta")
    void doProcess_threeDueRetries_processesEach() {
        WebhookDeliveryLog r1 = staleRetry();
        WebhookDeliveryLog r2 = staleRetry();
        WebhookDeliveryLog r3 = staleRetry();
        when(deliveryLogRepository.findDueRetries(
                eq(WebhookDeliveryStatus.RETRY), any(LocalDateTime.class), any(PageRequest.class)))
            .thenReturn(List.of(r1, r2, r3));

        scheduler.doProcess();

        // Birorta due row tushib qolmasligi kerak — har biri alohida replay qilinadi.
        verify(dispatcher, times(3)).redispatch(any(WebhookDeliveryLog.class));
        verify(dispatcher).redispatch(r1);
        verify(dispatcher).redispatch(r2);
        verify(dispatcher).redispatch(r3);
    }

    // =========================================================
    // retryOne — haqiqiy replay: dispatcher.redispatch()ga delegatsiya
    // (83d98de: eski "to'g'ridan DLQ" placeholder olib tashlandi, ADR-0012)
    // =========================================================

    @Test
    @DisplayName("retryOne() due row'ni redispatch()ga uzatadi — o'zi DLQ qilib tashlamaydi")
    void doProcess_retryOne_delegatesToRedispatch() {
        WebhookDeliveryLog stale = staleRetry();
        when(deliveryLogRepository.findDueRetries(any(), any(), any()))
            .thenReturn(List.of(stale));

        scheduler.doProcess();

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(dispatcher).redispatch(captor.capture());
        WebhookDeliveryLog replayed = captor.getValue();

        // Aynan o'sha due row uzatiladi — attempt_n/status'ni dispatcher hal qiladi.
        assertThat(replayed).isSameAs(stale);
        assertThat(replayed.getAttemptN()).isEqualTo(2);

        // Regressiya qo'riqchisi: scheduler retry'ni o'zi terminal DLQ qilmasligi shart
        // (aks holda ADR-0012 retry yana "birinchi due'da o'ladi" holatiga qaytadi).
        assertThat(stale.getStatus()).isEqualTo(WebhookDeliveryStatus.RETRY);
        verify(deliveryLogRepository, never()).save(any(WebhookDeliveryLog.class));
    }

    // =========================================================
    // Per-item exception isolation — one bad row doesn't kill the batch
    // =========================================================

    @Test
    @DisplayName("Bitta stale'da redispatch() otsa ham, qolgan retry'lar ishlanadi")
    void doProcess_perItemFailure_isolated() {
        WebhookDeliveryLog ok1 = staleRetry();
        WebhookDeliveryLog bad = staleRetry();
        WebhookDeliveryLog ok3 = staleRetry();
        when(deliveryLogRepository.findDueRetries(any(), any(), any()))
            .thenReturn(List.of(ok1, bad, ok3));
        doThrow(new RuntimeException("DB lock contention"))
            .when(dispatcher).redispatch(bad);

        assertThatCode(() -> scheduler.doProcess()).doesNotThrowAnyException();

        // bad urinildi-yu yiqildi; ok1 va ok3 baribir ishlandi (batch to'xtamaydi).
        verify(dispatcher).redispatch(ok1);
        verify(dispatcher).redispatch(bad);
        verify(dispatcher).redispatch(ok3);
    }

    // =========================================================
    // @Scheduled top-level exception swallowing
    // =========================================================

    @Test
    @DisplayName("processDueRetries() swallows ALL exceptions (scheduled task never propagates)")
    void processDueRetries_anyException_loggedNotPropagated() {
        when(deliveryLogRepository.findDueRetries(any(), any(), any()))
            .thenThrow(new RuntimeException("DB connection lost"));

        assertThatCode(() -> scheduler.processDueRetries()).doesNotThrowAnyException();
    }

    // =========================================================
    // Batch size respected (PageRequest.of(0, batchSize))
    // =========================================================

    @Test
    @DisplayName("doProcess() respects batchSize from config (default 50)")
    void doProcess_passesBatchSize() {
        ReflectionTestUtils.setField(scheduler, "batchSize", 10);
        when(deliveryLogRepository.findDueRetries(any(), any(), any()))
            .thenReturn(List.of());

        scheduler.doProcess();

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(deliveryLogRepository).findDueRetries(eq(WebhookDeliveryStatus.RETRY),
                any(LocalDateTime.class), pageCaptor.capture());
        assertThat(pageCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageCaptor.getValue().getPageNumber()).isZero();
    }

    // =========================================================
    // Retention cleanup — status bo'yicha farqlangan (SUCCESS −30d, FAILED −90d, DLQ saqlanadi)
    // =========================================================

    @Test
    @DisplayName("cleanupCompletedLogs() — SUCCESS cutoff now−30d, FAILED cutoff now−90d")
    void cleanupCompletedLogs_passesDifferentiatedCutoffs() {
        when(deliveryLogRepository.deleteByStatusAndCompletedAtBefore(any(), any(LocalDateTime.class)))
            .thenReturn(7);

        LocalDateTime before = LocalDateTime.now();
        scheduler.cleanupCompletedLogs();
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> successCutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(deliveryLogRepository)
            .deleteByStatusAndCompletedAtBefore(eq(WebhookDeliveryStatus.SUCCESS), successCutoff.capture());
        assertThat(successCutoff.getValue())
            .isAfterOrEqualTo(before.minusDays(30).minus(1, ChronoUnit.SECONDS))
            .isBeforeOrEqualTo(after.minusDays(30).plus(1, ChronoUnit.SECONDS));

        ArgumentCaptor<LocalDateTime> failedCutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(deliveryLogRepository)
            .deleteByStatusAndCompletedAtBefore(eq(WebhookDeliveryStatus.FAILED), failedCutoff.capture());
        assertThat(failedCutoff.getValue())
            .isAfterOrEqualTo(before.minusDays(90).minus(1, ChronoUnit.SECONDS))
            .isBeforeOrEqualTo(after.minusDays(90).plus(1, ChronoUnit.SECONDS));

        // DLQ hech qachon o'chirilmaydi
        verify(deliveryLogRepository, never())
            .deleteByStatusAndCompletedAtBefore(eq(WebhookDeliveryStatus.DLQ), any());
    }

    @Test
    @DisplayName("cleanupCompletedLogs() with 0 rows deleted → no exception, SUCCESS+FAILED ikki chaqiruv")
    void cleanupCompletedLogs_noRows_silent() {
        when(deliveryLogRepository.deleteByStatusAndCompletedAtBefore(any(), any(LocalDateTime.class)))
            .thenReturn(0);

        assertThatCode(() -> scheduler.cleanupCompletedLogs()).doesNotThrowAnyException();
        verify(deliveryLogRepository, times(2)).deleteByStatusAndCompletedAtBefore(any(), any());
    }

    // =========================================================
    // Helpers
    // =========================================================

    private WebhookDeliveryLog staleRetry() {
        WebhookDeliveryLog log = new WebhookDeliveryLog();
        log.setEventId(UUID.randomUUID());
        log.setEventType("classifier.updated");
        log.setUniversityCode("337");
        log.setAttemptN(2);
        log.setStatus(WebhookDeliveryStatus.RETRY);
        log.setNextRetryAt(LocalDateTime.now().minusSeconds(10));
        return log;
    }
}
