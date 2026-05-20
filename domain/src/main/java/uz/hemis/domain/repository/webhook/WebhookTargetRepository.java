package uz.hemis.domain.repository.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.webhook.WebhookTarget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * WebhookTarget repository — 254 OTM secret + tuning boshqaruvi.
 *
 * <p><strong>Active filter (2026-05-18 refactor):</strong> WebhookTarget'da
 * {@code active} field yo'q — bu {@code hemishe_e_university.active}'dan keladi.
 * Consumer JOIN orqali aktiv universitetlarni ajratadi.</p>
 *
 * @since ADR-0012
 */
@Repository
public interface WebhookTargetRepository extends JpaRepository<WebhookTarget, UUID> {

    /**
     * OTM bo'yicha target topish (consumer dispatch).
     * Active flag university tomonida — caller JOIN qilishi kerak.
     * {@code @SQLRestriction("deleted_at IS NULL")} entity'da — soft-deleted avtomatik filter.
     */
    Optional<WebhookTarget> findByUniversityCode(String universityCode);

    /**
     * Faqat aktiv universitet'lar target'lari (fanout consumer scan).
     * JOIN hemishe_e_university — active = true va delete_ts IS NULL.
     */
    @Query("""
        SELECT t FROM WebhookTarget t
        WHERE t.universityCode IN (
            SELECT u.code FROM University u
            WHERE u.active = true AND u.deleteTs IS NULL
        )
        """)
    List<WebhookTarget> findAllForActiveUniversities();

    /** Mavjud OTM tekshirish (CRUD validation). */
    boolean existsByUniversityCode(String universityCode);

    /** Aktiv target sanog'i (admin dashboard). */
    @Query("""
        SELECT COUNT(t) FROM WebhookTarget t
        WHERE t.universityCode IN (
            SELECT u.code FROM University u
            WHERE u.active = true AND u.deleteTs IS NULL
        )
        """)
    long countActive();
}
