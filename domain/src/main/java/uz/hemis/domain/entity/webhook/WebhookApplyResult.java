package uz.hemis.domain.entity.webhook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Univer-side apply natijasi (K2).
 *
 * <p>{@code webhook_delivery_log} faqat YETKAZISHNI (HTTP 2xx) kuzatadi. Univer event'ni
 * async apply qiladi — apply muvaffaqiyatsiz bo'lsa markaz bilmасdi ("delivered != applied").
 * Endi univer {@code ApplyHemisEventJob} apply tugagach markazga HMAC-imzolangan ack POST qiladi
 * va natija shu jadvalga yoziladi. Bir (event_id, university_code) = bitta row (oxirgi ack upsert).</p>
 *
 * @since ADR-0012 (K2)
 */
@Entity
@Table(name = "webhook_apply_result")
@Getter
@Setter
@NoArgsConstructor
public class WebhookApplyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "university_code", nullable = false, length = 10)
    private String universityCode;

    /** Univer-side apply natijasi: {@code 'applied'} yoki {@code 'failed'} (DB CHECK). */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** Univer apply tugagan vaqt (applied bo'lsa). */
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    /** Univer apply xatosi (failed bo'lsa, qisqa sabab). */
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    /** Markaz ack qabul qilgan vaqt. */
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;
}
