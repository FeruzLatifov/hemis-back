package uz.hemis.service.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WebhookRetryScheduler}.
 *
 * <p><strong>Scope:</strong> due-retry polling, per-item exception isolation,
 * placeholder retryOne side effect, retention cleanup cutoff math, and
 * {@code @Scheduled} top-level exception swallowing.</p>
 *
 * <p>Pure Mockito — no Spring context, no DB.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookRetryScheduler Tests")
class WebhookRetrySchedulerTest {

    @Mock private WebhookDeliveryLogRepository deliveryLogRepository;
    @Mock private WebhookDispatcher dispatcher;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private WebhookRetryScheduler scheduler;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(scheduler, "batchSize", 50);
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
    @DisplayName("doProcess() with 3 due retries → save() called 3 times (one per stale)")
    void doProcess_threeDueRetries_processesEach() {
        WebhookDeliveryLog r1 = staleRetry();
        WebhookDeliveryLog r2 = staleRetry();
        WebhookDeliveryLog r3 = staleRetry();
        when(deliveryLogRepository.findDueRetries(
                eq(WebhookDeliveryStatus.RETRY), any(LocalDateTime.class), any(PageRequest.class)))
            .thenReturn(List.of(r1, r2, r3));

        scheduler.doProcess();

        verify(deliveryLogRepository, times(3)).save(any(WebhookDeliveryLog.class));
    }

    // =========================================================
    // Placeholder retryOne side effect — stale becomes DLQ
    // =========================================================

    @Test
    @DisplayName("retryOne placeholder marks stale as DLQ with placeholder error message")
    void doProcess_retryOnePlaceholder_marksDlq() {
        WebhookDeliveryLog stale = staleRetry();
        when(deliveryLogRepository.findDueRetries(any(), any(), any()))
            .thenReturn(List.of(stale));

        scheduler.doProcess();

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(deliveryLogRepository).save(captor.capture());
        WebhookDeliveryLog saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(WebhookDeliveryStatus.DLQ);
        assertThat(saved.getErrorMessage()).contains("Sprint 4");
        assertThat(saved.getCompletedAt()).isNotNull();
    }

    // =========================================================
    // Per-item exception isolation — one bad row doesn't kill the batch
    // =========================================================

    @Test
    @DisplayName("If save() throws for one stale, other retries still processed")
    void doProcess_perItemFailure_isolated() {
        WebhookDeliveryLog ok1 = staleRetry();
        WebhookDeliveryLog bad = staleRetry();
        WebhookDeliveryLog ok3 = staleRetry();
        when(deliveryLogRepository.findDueRetries(any(), any(), any()))
            .thenReturn(List.of(ok1, bad, ok3));
        when(deliveryLogRepository.save(bad))
            .thenThrow(new RuntimeException("DB lock contention"));

        assertThatCode(() -> scheduler.doProcess()).doesNotThrowAnyException();

        // ok1 and ok3 still saved; bad attempted but failed
        verify(deliveryLogRepository).save(ok1);
        verify(deliveryLogRepository).save(bad);
        verify(deliveryLogRepository).save(ok3);
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
    // Retention cleanup — cutoff is now − 60 days
    // =========================================================

    @Test
    @DisplayName("cleanupCompletedLogs() passes cutoff = now − 60 days to repository")
    void cleanupCompletedLogs_passesCorrectCutoff() {
        when(deliveryLogRepository.deleteCompletedBefore(any(LocalDateTime.class)))
            .thenReturn(7);

        LocalDateTime before = LocalDateTime.now();
        scheduler.cleanupCompletedLogs();
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(deliveryLogRepository).deleteCompletedBefore(cutoff.capture());

        LocalDateTime expectedMin = before.minusDays(60);
        LocalDateTime expectedMax = after.minusDays(60);
        assertThat(cutoff.getValue())
            .isAfterOrEqualTo(expectedMin.minus(1, ChronoUnit.SECONDS))
            .isBeforeOrEqualTo(expectedMax.plus(1, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("cleanupCompletedLogs() with 0 rows deleted → no exception, no extra DB call")
    void cleanupCompletedLogs_noRows_silent() {
        when(deliveryLogRepository.deleteCompletedBefore(any(LocalDateTime.class)))
            .thenReturn(0);

        assertThatCode(() -> scheduler.cleanupCompletedLogs()).doesNotThrowAnyException();
        verify(deliveryLogRepository, times(1)).deleteCompletedBefore(any());
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
