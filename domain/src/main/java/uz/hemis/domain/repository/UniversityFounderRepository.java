package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.vo.Pinfl;
import uz.hemis.domain.entity.university.UniversityFounder;

import java.util.List;
import java.util.UUID;

/**
 * Repository for UniversityFounder entity.
 *
 * <p>Sync uses DELETE+INSERT pattern — there is no historical tracking on the
 * entity. Past founders are recorded only in {@code hemis_audit.activity_log}.
 * All queries automatically filter {@code deleted_at IS NULL} via the entity-level
 * Hibernate {@code @SQLRestriction}.</p>
 *
 * @see UniversityFounder
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityFounderRepository extends JpaRepository<UniversityFounder, UUID> {

    @EntityGraph(attributePaths = {"employee", "organization"})
    List<UniversityFounder> findByUniversityCode(String universityCode);

    /**
     * Plain-string accessor — Spring Data derived query. Caller layer
     * (service) where possible should use {@link #findByEmployeePinfl(Pinfl)} for
     * compile-time validation (14-digit format).
     */
    List<UniversityFounder> findByEmployee_Pinfl(String pinfl);

    /**
     * Type-safe overload — accepts validated {@link Pinfl} VO. Pre-construction
     * format check eliminates "garbage in" lookups.
     */
    default List<UniversityFounder> findByEmployeePinfl(Pinfl pinfl) {
        return findByEmployee_Pinfl(pinfl == null ? null : pinfl.value());
    }

    List<UniversityFounder> findByEmployee_Id(UUID employeeId);
}
