package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.DeleteSpecification;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.University;

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
 * <p><strong>NO-DELETE Constraint (NDG) — enforced, not merely documented:</strong></p>
 * <ul>
 *   <li>"No delete methods are <em>defined</em>" was never a guarantee: {@code JpaRepository} and
 *       {@code JpaSpecificationExecutor} <em>inherit</em> eleven hard-delete entry points into this
 *       interface, and any of them would issue a physical {@code DELETE FROM hemishe_e_university}.</li>
 *   <li>All eleven are therefore overridden below as {@code default} methods that throw
 *       {@link UnsupportedOperationException}: five from {@code CrudRepository}
 *       ({@code delete}, {@code deleteById}, {@code deleteAll()}, {@code deleteAll(Iterable)},
 *       {@code deleteAllById}), four from {@code JpaRepository}
 *       ({@code deleteInBatch}, {@code deleteAllInBatch()}, {@code deleteAllInBatch(Iterable)},
 *       {@code deleteAllByIdInBatch}) and two bulk overloads from {@code JpaSpecificationExecutor}
 *       ({@code delete(DeleteSpecification)}, {@code delete(PredicateSpecification)}).</li>
 *   <li>Each is also {@code @Deprecated}, so an IDE strikes the call through at the keystroke
 *       rather than at runtime.</li>
 *   <li>Soft delete ONLY: {@code UniversityRegistryService.deleteUniversity(code)} stamps
 *       {@code delete_ts}; {@code restoreUniversity(code)} clears it.</li>
 *   <li>Defence in depth at the database layer: migration {@code M016} converts the last two
 *       {@code ON DELETE CASCADE} children of {@code hemishe_e_university}
 *       ({@code university_founder}, {@code university_profile}) to {@code RESTRICT}, so a manual
 *       {@code psql} DELETE is refused too.</li>
 * </ul>
 *
 * <p><strong>Soft Delete Filtering:</strong></p>
 * <ul>
 *   <li>@SQLRestriction("delete_ts IS NULL") on University entity</li>
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
    // NO DELETE METHODS — inherited hard deletes are sealed shut
    // =====================================================
    // Physical DELETE is PROHIBITED (NDG). Not declaring these methods is not enough:
    // they are inherited from CrudRepository / JpaRepository / JpaSpecificationExecutor and
    // Spring Data implements every one of them. Overriding each as a default method that
    // throws replaces a comment with a compiler- and runtime-enforced rule.
    //
    // If a genuine hard delete is ever needed (it should not be — 95 foreign keys point at
    // hemishe_e_university), write an explicit, named, reviewed native statement; do not
    // re-open these.
    // =====================================================

    /**
     * Rejection shared by every disabled hard-delete entry point.
     *
     * @param method the inherited signature that was called
     * @return the exception to throw (never returns normally at the call site)
     */
    private static UnsupportedOperationException hardDeleteProhibited(String method) {
        return new UnsupportedOperationException(
                "UniversityRepository." + method + " is disabled: hemishe_e_university is soft-delete only \u2014 "
                        + "use UniversityRegistryService.deleteUniversity(code) to hide a university "
                        + "and UniversityRegistryService.restoreUniversity(code) to bring it back.");
    }

    // ---- CrudRepository (5) ----

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void delete(University entity) {
        throw hardDeleteProhibited("delete(University)");
    }

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void deleteById(String code) {
        throw hardDeleteProhibited("deleteById(String)");
    }

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void deleteAll() {
        throw hardDeleteProhibited("deleteAll()");
    }

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void deleteAll(Iterable<? extends University> entities) {
        throw hardDeleteProhibited("deleteAll(Iterable)");
    }

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void deleteAllById(Iterable<? extends String> codes) {
        throw hardDeleteProhibited("deleteAllById(Iterable)");
    }

    // ---- JpaRepository (4) ----

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void deleteInBatch(Iterable<University> entities) {
        throw hardDeleteProhibited("deleteInBatch(Iterable)");
    }

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void deleteAllInBatch() {
        throw hardDeleteProhibited("deleteAllInBatch()");
    }

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void deleteAllInBatch(Iterable<University> entities) {
        throw hardDeleteProhibited("deleteAllInBatch(Iterable)");
    }

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default void deleteAllByIdInBatch(Iterable<String> codes) {
        throw hardDeleteProhibited("deleteAllByIdInBatch(Iterable)");
    }

    // ---- JpaSpecificationExecutor (2) ----
    // Bulk "DELETE ... WHERE <spec>" overloads added in Spring Data JPA 4.x. They are the
    // easiest of the eleven to miss: they take a Specification, not an entity or an id.

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default long delete(DeleteSpecification<University> spec) {
        throw hardDeleteProhibited("delete(DeleteSpecification)");
    }

    /** @deprecated hard delete is prohibited; use the service-layer soft delete. */
    @Override
    @Deprecated
    default long delete(PredicateSpecification<University> spec) {
        throw hardDeleteProhibited("delete(PredicateSpecification)");
    }

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

//    @Query("select (count(u) > 0) from University u where u.code = :code")
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

    /**
     * Find university by code including soft-deleted records.
     * Bypasses @SQLRestriction to find deleted universities for restore.
     *
     * @param code university code
     * @return Optional University (including deleted)
     */
    @Query(value = "SELECT * FROM hemishe_e_university WHERE code = :code", nativeQuery = true)
    Optional<University> findByIdIncludingDeleted(@Param("code") String code);

    /**
     * Find all soft-deleted universities, newest deletion first (the "Deleted" bin).
     *
     * <p>Native by necessity: {@code @SQLRestriction("delete_ts IS NULL")} on the entity is appended
     * to every JPQL/Criteria query, so a JPA query for these rows can only ever return empty. The
     * native statement bypasses the restriction — the same escape hatch
     * {@link #findByIdIncludingDeleted(String)} uses — and states the inverse predicate itself.</p>
     *
     * @return List of soft-deleted universities (newest first)
     */
    @Query(value = "SELECT * FROM hemishe_e_university WHERE delete_ts IS NOT NULL ORDER BY delete_ts DESC",
           nativeQuery = true)
    List<University> findAllDeleted();
}
