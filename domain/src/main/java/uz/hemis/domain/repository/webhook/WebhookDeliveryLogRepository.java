package uz.hemis.domain.repository.webhook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.webhook.WebhookDeliveryLog;
import uz.hemis.domain.entity.webhook.WebhookDeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * WebhookDeliveryLog repository — har attempt audit.
 *
 * @since ADR-0012
 */
@Repository
public interface WebhookDeliveryLogRepository extends JpaRepository<WebhookDeliveryLog, UUID> {

    /** Event uchun barcha attempt'lar (audit view). */
    List<WebhookDeliveryLog> findByEventIdOrderByAttemptNAsc(UUID eventId);

    /** Target uchun delivery tarix (per-OTM admin UI). */
    Page<WebhookDeliveryLog> findByTargetIdOrderByDispatchedAtDesc(UUID targetId, Pageable pageable);

    /**
     * Retry queue — keyingi retry vaqti kelgan event'lar.
     * Retry poller bu queryni har sekund chaqiradi.
     */
    @Query("""
            SELECT l FROM WebhookDeliveryLog l
             WHERE l.status = :status
               AND l.nextRetryAt <= :now
             ORDER BY l.nextRetryAt ASC
            """)
    List<WebhookDeliveryLog> findDueRetries(
            @Param("status") WebhookDeliveryStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /** DLQ list (admin manual review). */
    Page<WebhookDeliveryLog> findByStatusOrderByDispatchedAtDesc(WebhookDeliveryStatus status, Pageable pageable);

    /**
     * Per-OTM SLA metrics — oxirgi 24 soatda success/failure ratio.
     */
    @Query("""
            SELECT l.status, COUNT(l)
              FROM WebhookDeliveryLog l
             WHERE l.universityCode = :universityCode
               AND l.dispatchedAt >= :since
             GROUP BY l.status
            """)
    List<Object[]> aggregateStatusByUniversity(
            @Param("universityCode") String universityCode,
            @Param("since") LocalDateTime since
    );

    /** Latest attempt for (event, target) pair — duplicate detection. */
    @Query("""
            SELECT l FROM WebhookDeliveryLog l
             WHERE l.eventId = :eventId
               AND l.target.id = :targetId
             ORDER BY l.attemptN DESC
            """)
    List<WebhookDeliveryLog> findLatestAttempt(
            @Param("eventId") UUID eventId,
            @Param("targetId") UUID targetId,
            Pageable pageable
    );

    /**
     * Retention cleanup — status bo'yicha farqlangan. DLQ o'chirilmaydi (manual review uchun saqlanadi).
     * Scheduler ikki marta chaqiradi: SUCCESS (qisqa retention), FAILED (uzoqroq retention).
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("""
            DELETE FROM WebhookDeliveryLog l
             WHERE l.status = :status
               AND l.completedAt < :cutoff
            """)
    int deleteByStatusAndCompletedAtBefore(
            @Param("status") WebhookDeliveryStatus status,
            @Param("cutoff") LocalDateTime cutoff);
}
