package uz.hemis.domain.repository.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.outbox.OutboxEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OutboxEvent repository — Transactional Outbox poller uchun.
 *
 * <p><strong>Multi-instance safety:</strong> {@link #pollUnpublishedForUpdate} method
 * {@code FOR UPDATE SKIP LOCKED} ishlatadi — bir nechta hemis-back instance bir vaqtda
 * poll qilsa, har biri o'z partition'ini oladi (lock conflict yo'q).</p>
 *
 * @since ADR-0010
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Pending event'larni poll qilish (Kafka'ga jo'natish uchun).
     *
     * <p>Native SQL — {@code FOR UPDATE SKIP LOCKED} JPQL'da qo'llab-quvvatlanmaydi.
     * Multi-instance safe — boshqa instance lock qilgan row'larni o'tkazib yuboradi.</p>
     *
     * @param limit batch size (tipik 100)
     * @return unpublished event'lar, occurred_at bo'yicha eski → yangi tartibda
     */
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE published_at IS NULL
              AND retry_count < 100
            ORDER BY occurred_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> pollUnpublishedForUpdate(@Param("limit") int limit);

    /**
     * Event'ni "published" deb belgilash (atomic UPDATE).
     *
     * @param eventId outbox row ID
     * @return 1 = updated, 0 = not found (race condition — boshqa instance allaqachon yangiladi)
     */
    @Modifying
    @Query("""
            UPDATE OutboxEvent e
               SET e.publishedAt = CURRENT_TIMESTAMP
             WHERE e.id = :eventId
               AND e.publishedAt IS NULL
            """)
    int markPublished(@Param("eventId") UUID eventId);

    /**
     * Retry attempt — error saqlash va retry_count oshirish.
     */
    @Modifying
    @Query("""
            UPDATE OutboxEvent e
               SET e.retryCount = e.retryCount + 1,
                   e.lastError  = :error
             WHERE e.id = :eventId
            """)
    int markRetry(@Param("eventId") UUID eventId, @Param("error") String error);

    /**
     * Per-aggregate event history (audit/replay uchun).
     * api-web admin UI: "Bu employee uchun barcha event'lar".
     */
    @Query("""
            SELECT e FROM OutboxEvent e
             WHERE e.aggregateType = :aggregateType
               AND e.aggregateId = :aggregateId
             ORDER BY e.occurredAt DESC
            """)
    List<OutboxEvent> findByAggregate(
            @Param("aggregateType") String aggregateType,
            @Param("aggregateId") String aggregateId,
            Pageable pageable
    );

    /**
     * DLQ candidates — retry tugagan event'lar (manual admin review).
     */
    @Query("""
            SELECT e FROM OutboxEvent e
             WHERE e.publishedAt IS NULL
               AND e.retryCount >= :maxRetries
             ORDER BY e.occurredAt DESC
            """)
    List<OutboxEvent> findDlqCandidates(@Param("maxRetries") int maxRetries, Pageable pageable);

    /**
     * Eski published event'lar (retention cleanup — 30 kun).
     * Scheduled task har kuni chaqiriladi.
     */
    @Modifying
    @Query("""
            DELETE FROM OutboxEvent e
             WHERE e.publishedAt IS NOT NULL
               AND e.publishedAt < :cutoff
            """)
    int deletePublishedBefore(@Param("cutoff") LocalDateTime cutoff);

    /**
     * Correlation ID bo'yicha event chain (distributed tracing).
     */
    List<OutboxEvent> findByCorrelationIdOrderByOccurredAtAsc(String correlationId);
}
