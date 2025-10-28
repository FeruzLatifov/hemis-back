package uz.hemis.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.hemis.admin.entity.University;

import java.util.Optional;

/**
 * Repository for University entity (basic version)
 *
 * Provides database access for university data
 * Note: Full university repository is in domain module
 */
@Repository
public interface UniversityRepository extends JpaRepository<University, String> {

    /**
     * Find active university by code
     *
     * @param code University code
     * @return Optional university
     */
    Optional<University> findByCodeAndDeletedAtIsNull(String code);

    /**
     * Find university by code (including soft-deleted)
     *
     * @param code University code
     * @return Optional university
     */
    Optional<University> findByCode(String code);

    /**
     * Check if university code exists (active only)
     *
     * @param code University code
     * @return true if exists and active
     */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /**
     * Find active university by TIN
     *
     * @param tin Tax Identification Number
     * @return Optional university
     */
    Optional<University> findByTinAndDeletedAtIsNull(String tin);

    /**
     * Count active universities
     *
     * @return Number of active universities
     */
    long countByDeletedAtIsNull();
}
