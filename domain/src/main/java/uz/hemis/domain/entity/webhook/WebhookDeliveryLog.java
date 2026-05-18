package uz.hemis.domain.entity.webhook;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Webhook delivery audit log — har attempt uchun bitta row.
 *
 * <p>Bir {@code event_id} × N {@code attempt_n} = N row. Replay, troubleshooting,
 * SLA hisoboti uchun ishlatiladi.</p>
 *
 * <p><strong>Lifecycle:</strong></p>
 * <pre>
 *   1. Consumer event'ni oladi → row yaratish (PENDING)
 *   2. RestClient.post() → 200 OK → SUCCESS
 *                       → 5xx / timeout → RETRY (next_retry_at exponential backoff)
 *                       → 4xx → FAILED (terminal)
 *   3. attempt_n &gt; max_retries → DLQ
 * </pre>
 *
 * @see V016_create_webhook_infrastructure.sql
 * @since ADR-0012
 */
@Entity
@Table(name = "webhook_delivery_log")
@Getter
@Setter
@NoArgsConstructor
public class WebhookDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Outbox event reference (soft FK — outbox 30 kun keyin retention'da o'chishi mumkin,
     * delivery log esa audit uchun saqlanadi).
     */
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    /** Denormalized event_type (filter performance). */
    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    /** Target reference (real FK — RESTRICT — target'ni o'chirish blok qilinadi). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private WebhookTarget target;

    /** Denormalized university_code — admin UI per-OTM filter uchun. */
    @Column(name = "university_code", nullable = false, length = 10)
    private String universityCode;

    /** Attempt raqami (1 = birinchi, max 10). */
    @Column(name = "attempt_n", nullable = false)
    private Integer attemptN = 1;

    /** HTTP javob status. {@code NULL} = network error (timeout, connection refused). */
    @Column(name = "http_status")
    private Integer httpStatus;

    /** Univer response body (max 4KB — application layer truncates). */
    @Column(name = "response_body", columnDefinition = "text")
    private String responseBody;

    /** Exception message yoki network error tafsiloti. */
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    /** Request latency (millisekund). */
    @Column(name = "duration_ms")
    private Integer durationMs;

    /** Joriy state — {@link WebhookDeliveryStatus}. DB lowercase, converter orqali. */
    @Convert(converter = WebhookDeliveryStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private WebhookDeliveryStatus status = WebhookDeliveryStatus.PENDING;

    /** Yuborish boshlangan vaqt (DB default {@code CURRENT_TIMESTAMP}). */
    @Column(name = "dispatched_at", nullable = false)
    private LocalDateTime dispatchedAt;

    /** Yuborish tugagan vaqt (success/failed). */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Keyingi retry vaqti (exponential backoff: 1s, 5s, 30s, 5min, 1h). */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @PrePersist
    void prePersist() {
        if (dispatchedAt == null) {
            dispatchedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = WebhookDeliveryStatus.PENDING;
        }
        if (attemptN == null) {
            attemptN = 1;
        }
    }

    /** SUCCESS terminal — completed_at o'rnatish. */
    public void markSuccess(int httpStatus, String responseBody, int durationMs) {
        this.status = WebhookDeliveryStatus.SUCCESS;
        this.httpStatus = httpStatus;
        this.responseBody = truncate(responseBody, 4096);
        this.durationMs = durationMs;
        this.completedAt = LocalDateTime.now();
    }

    /** FAILED terminal — 4xx (payload xato, retry mantiqsiz). */
    public void markFailed(int httpStatus, String responseBody, String errorMessage) {
        this.status = WebhookDeliveryStatus.FAILED;
        this.httpStatus = httpStatus;
        this.responseBody = truncate(responseBody, 4096);
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    /** RETRY — exponential backoff. */
    public void markRetry(Integer httpStatus, String errorMessage, LocalDateTime nextRetryAt) {
        this.status = WebhookDeliveryStatus.RETRY;
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
        this.nextRetryAt = nextRetryAt;
    }

    /** DLQ — retry tugadi. */
    public void markDlq(String errorMessage) {
        this.status = WebhookDeliveryStatus.DLQ;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max - 16) + "...[truncated]";
    }
}
