package uz.hemis.domain.repository.webhook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.webhook.WebhookApplyResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * WebhookApplyResult repository — univer-side apply natijasi (K2).
 *
 * @since ADR-0012 (K2)
 */
@Repository
public interface WebhookApplyResultRepository extends JpaRepository<WebhookApplyResult, UUID> {

    /** Upsert uchun: mavjud natijani (event × OTM) topish. */
    Optional<WebhookApplyResult> findByEventIdAndUniversityCode(UUID eventId, String universityCode);

    /** Admin: status bo'yicha (masalan 'failed') — "qaysi OTM da apply fail bo'ldi". */
    Page<WebhookApplyResult> findByStatusOrderByReportedAtDesc(String status, Pageable pageable);

    /** Admin: barcha apply natijalar (eng yangi avval). */
    Page<WebhookApplyResult> findAllByOrderByReportedAtDesc(Pageable pageable);

    /** Bitta event bo'yicha barcha OTM natijasi (event drill-down). */
    List<WebhookApplyResult> findByEventIdOrderByUniversityCodeAsc(UUID eventId);
}
