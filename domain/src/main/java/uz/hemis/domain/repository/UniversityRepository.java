package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.University;

import java.util.List;
import java.util.Optional;

/**
 * Repository for University entity
 *
 * <p><strong>CRITICAL - VARCHAR Primary Key:</strong></p>
 * <ul>
 *   <li>PK Type: String (code) - NOT UUID!</li>
 *   <li>JpaRepository<University, String></li>
 * </ul>
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong></p>
 * <ul>
 *   <li>NO delete() methods defined</li>
 *   <li>NO deleteById() methods defined</li>
 *   <li>NO deleteAll() methods defined</li>
 *   <li>Soft delete ONLY (set deleteTs in service layer)</li>
 * </ul>
 *
 * <p><strong>Soft Delete Filtering:</strong></p>
 * <ul>
 *   <li>@Where(clause = "delete_ts IS NULL") on University entity</li>
 *   <li>All queries automatically filter deleted records</li>
 * </ul>
 *
 * @see University
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityRepository extends JpaRepository<University, String>, JpaSpecificationExecutor<University> {

    // =====================================================
    // NO DELETE METHODS
    // =====================================================
    // Physical DELETE operations are PROHIBITED (NDG).
    // Use service layer soft delete (set deleteTs) instead.
    // =====================================================

    // =====================================================
    // Find by Business Fields
    // =====================================================

    /**
     * Find university by TIN
     *
     * @param tin Tax Identification Number
     * @return Optional University
     */
    Optional<University> findByTin(String tin);

    /**
     * Find universities by name (partial match, case-insensitive)
     *
     * @param name university name (partial)
     * @param pageable pagination
     * @return Page of universities
     */
    @Query("SELECT u FROM University u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<University> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find active universities
     *
     * @param pageable pagination
     * @return Page of active universities
     */
    @Query("SELECT u FROM University u WHERE u.active = true")
    Page<University> findByActiveTrue(Pageable pageable);

    /**
     * Find universities by type
     *
     * @param typeCode university type code
     * @param pageable pagination
     * @return Page of universities
     */
    @Query("SELECT u FROM University u WHERE u.universityType = :typeCode")
    Page<University> findByUniversityType(@Param("typeCode") String typeCode, Pageable pageable);

    /**
     * Find universities by ownership
     *
     * @param ownershipCode ownership code
     * @param pageable pagination
     * @return Page of universities
     */
    @Query("SELECT u FROM University u WHERE u.ownership = :ownershipCode")
    Page<University> findByOwnership(@Param("ownershipCode") String ownershipCode, Pageable pageable);

    /**
     * Find universities by parent university
     *
     * @param parentCode parent university code
     * @return List of child universities
     */
    @Query("SELECT u FROM University u WHERE u.parentUniversity = :parentCode")
    List<University> findByParentUniversity(@Param("parentCode") String parentCode);

    /**
     * Find universities by SOATO region
     *
     * @param soatoRegion SOATO region code
     * @param pageable pagination
     * @return Page of universities
     */
    @Query("SELECT u FROM University u WHERE u.soatoRegion = :soatoRegion")
    Page<University> findBySoatoRegion(@Param("soatoRegion") String soatoRegion, Pageable pageable);

    // =====================================================
    // Existence Checks
    // =====================================================

    /**
     * Check if university exists by code (excluding deleted)
     *
     * @param code university code
     * @return true if exists
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM University u WHERE u.code = :code")
    boolean existsByCode(@Param("code") String code);

    /**
     * Check if university with TIN exists (excluding current code)
     *
     * @param tin university TIN
     * @param code current university code (to exclude from check)
     * @return true if exists
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM University u " +
           "WHERE u.tin = :tin AND u.code != :code")
    boolean existsByTinAndCodeNot(@Param("tin") String tin, @Param("code") String code);

    // =====================================================
    // Statistics
    // =====================================================

    /**
     * Count active universities
     *
     * @return count of active universities
     */
    @Query("SELECT COUNT(u) FROM University u WHERE u.active = true")
    long countActiveUniversities();

    /**
     * Count universities by type
     *
     * @param typeCode university type code
     * @return count
     */
    @Query("SELECT COUNT(u) FROM University u WHERE u.universityType = :typeCode")
    long countByUniversityType(@Param("typeCode") String typeCode);

    /**
     * Count universities by ownership
     *
     * @param ownershipCode ownership code
     * @return count
     */
    @Query("SELECT COUNT(u) FROM University u WHERE u.ownership = :ownershipCode")
    long countByOwnership(@Param("ownershipCode") String ownershipCode);

    // =====================================================
    // Custom Queries
    // =====================================================

    /**
     * Find all active universities with student portal access
     *
     * @return List of universities with student portal
     */
    @Query("SELECT u FROM University u WHERE u.active = true AND u.studentUrl IS NOT NULL")
    List<University> findActiveUniversitiesWithStudentPortal();

    /**
     * Find universities by activity status
     *
     * @param activityStatus university activity status code
     * @param pageable pagination
     * @return Page of universities
     */
    @Query("SELECT u FROM University u WHERE u.universityActivityStatus = :activityStatus")
    Page<University> findByActivityStatus(@Param("activityStatus") String activityStatus, Pageable pageable);
}
