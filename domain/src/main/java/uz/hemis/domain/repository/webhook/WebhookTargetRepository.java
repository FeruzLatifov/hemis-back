package uz.hemis.domain.repository.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.webhook.WebhookTarget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * WebhookTarget repository — 224 OTM URL boshqaruvi.
 *
 * @since ADR-0012
 */
@Repository
public interface WebhookTargetRepository extends JpaRepository<WebhookTarget, UUID> {

    /**
     * OTM bo'yicha aktiv target topish (consumer fanout uchun).
     * {@code @SQLRestriction("deleted_at IS NULL")} entity'da — soft-deleted avtomatik filter.
     */
    Optional<WebhookTarget> findByUniversityCodeAndActiveTrue(String universityCode);

    /** Barcha aktiv target'lar (fanout consumer scan). */
    List<WebhookTarget> findAllByActiveTrue();

    /** OTM bo'yicha target (active/inactive — admin UI). */
    Optional<WebhookTarget> findByUniversityCode(String universityCode);

    /** Mavjud OTM tekshirish (CRUD validation). */
    boolean existsByUniversityCode(String universityCode);

    /**
     * Soft-deleted bo'lmagan barcha target sanog'i (admin dashboard).
     */
    @Query("SELECT COUNT(t) FROM WebhookTarget t WHERE t.active = true")
    long countActive();
}
