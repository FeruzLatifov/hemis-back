package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.classifier.HSpeciality;
import uz.hemis.domain.entity.classifier.ReviewStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the unified speciality classifier ({@code h_speciality}).
 *
 * <p>Supports the education-type filter (a {@code hemishe_h_education_type.code}: '11'=Bakalavr,
 * '12'=Magistr), the {@link ReviewStatus} ("to'g'rilash kerak") filter, and the parent tree.</p>
 */
@Repository
@Transactional(readOnly = true)
public interface HSpecialityRepository extends JpaRepository<HSpeciality, UUID> {

    /**
     * Exact-match candidates for the "already exists" create warning: active rows whose
     * {@code code} equals {@code code} OR whose folded {@code nameSearch} equals {@code nameSearch},
     * scoped to {@code educationType} (null = all types). Equality predicates use the existing partial
     * {@code idx_h_speciality_code} + {@code idx_h_speciality_search} indexes. Advisory ONLY — code
     * is intentionally NON-unique, so this never blocks a create. Both params null ⇒ no rows.
     * {@code CAST(:p AS string) IS NOT NULL} dodges the untyped-bind (as {@code search}/{@code findRoots} do).
     */
    @Query("SELECT s FROM HSpeciality s WHERE s.active = true " +
           "AND s.educationType = COALESCE(:educationType, s.educationType) " +
           "AND ((CAST(:code AS string) IS NOT NULL AND s.code = :code) " +
           "  OR (CAST(:nameSearch AS string) IS NOT NULL AND s.nameSearch = :nameSearch))")
    List<HSpeciality> findDuplicates(@Param("educationType") String educationType,
                                     @Param("code") String code,
                                     @Param("nameSearch") String nameSearch);

    /**
     * Active literal twins for the manual-add year-merge: same {@code educationType}, same folded
     * {@code nameSearch}, and same {@code code} (a null param matches a null code). Parent-independent
     * — the same code+name is one speciality, so a create for it merges the new year into the existing
     * row instead of duplicating. Code-only or name-only overlaps are NOT twins (recoding keeps the
     * name; code reuse keeps the code), so they create a fresh row. The {@code CAST(:code AS string)}
     * guard dodges the untyped-null bind (as {@code findDuplicates} does).
     */
    @Query("SELECT s FROM HSpeciality s WHERE s.active = true " +
           "AND s.educationType = :educationType " +
           "AND s.nameSearch = :nameSearch " +
           "AND ((CAST(:code AS string) IS NULL AND s.code IS NULL) OR s.code = :code)")
    List<HSpeciality> findExactTwins(@Param("educationType") String educationType,
                                     @Param("code") String code,
                                     @Param("nameSearch") String nameSearch);

    /**
     * Root nodes (parent IS NULL), optionally scoped to one education type.
     *
     * <p>The nullable education-type filter uses {@code = COALESCE(:param, column)} rather than
     * {@code (:param IS NULL OR column = :param)}: COALESCE lets PostgreSQL infer the bind type from
     * the sibling column and keeps a single index-friendly predicate. All rows carry education_type,
     * so the semantics match (null param ⇒ every type).</p>
     */
    @Query("SELECT s FROM HSpeciality s WHERE s.parent IS NULL AND s.active = true " +
           "AND s.educationType = COALESCE(:educationType, s.educationType) ORDER BY s.code ASC")
    List<HSpeciality> findRoots(@Param("educationType") String educationType);

    /**
     * EVERY LIVE direct child of a node — deactivated rows included — ordered by code.
     *
     * <p>The single definition of "has children" in this classifier: the delete guard, the
     * level-change guard, and the detail view all read it, so the UI never offers an action the
     * server then refuses. An {@code active = false} filter would be wrong for all three: a
     * deactivated child is still visible to the admin and is still dragged along by a level change.
     * The returned rows also name the blockers back to the admin ("re-place these first").</p>
     *
     * <p><strong>M013 — soft-deleted children do NOT block.</strong> {@code @SQLRestriction} hides
     * them here, which is deliberate: the admin sees a childless node, so the server must agree.
     * Note this guard now has NO database backstop — {@code fk_h_speciality_parent}
     * ({@code ON DELETE RESTRICT}) never fires against the UPDATE a soft delete performs, so it can
     * no longer turn a missed check into a constraint error. The reverse direction (restoring a
     * child under a deleted parent) is closed by {@code SPECIALITY_RESTORE_PARENT_DELETED}.</p>
     */
    @Query("SELECT s FROM HSpeciality s WHERE s.parent.id = :parentId ORDER BY s.code ASC")
    List<HSpeciality> findAllChildren(@Param("parentId") UUID parentId);

    List<HSpeciality> findByEducationTypeAndActiveTrue(String educationType);

    List<HSpeciality> findByReviewStatus(ReviewStatus reviewStatus);


    /**
     * Distinct edition years available across the active classifier, newest first — the source
     * for the year-filter dropdown. Optionally scoped to one education type (bachelor/master).
     */
    @Query("SELECT DISTINCT y.year FROM HSpecialityYear y WHERE y.specialityId IN " +
           "(SELECT s.id FROM HSpeciality s WHERE s.active = true " +
           "AND s.educationType = COALESCE(:educationType, s.educationType)) " +
           "ORDER BY y.year DESC")
    List<Integer> findDistinctYears(@Param("educationType") String educationType);

    /** Full active set for one education type — service builds the tree in memory. */
    @Query("SELECT s FROM HSpeciality s WHERE s.active = true " +
           "AND s.educationType = COALESCE(:educationType, s.educationType) " +
           "AND s.reviewStatus = COALESCE(:reviewStatus, s.reviewStatus)")
    List<HSpeciality> findAllFiltered(@Param("educationType") String educationType,
                                      @Param("reviewStatus") ReviewStatus reviewStatus);

    long countByReviewStatus(ReviewStatus reviewStatus);

    /**
     * Distributable snapshot for OTM pull/push — the APPROVED, code-bearing, active rows only
     * (excludes the 53 NEEDS_REVIEW incl. the 15 code-less; {@code code IS NOT NULL} guarantees the
     * upsert key Univer needs). The SAME predicate gates the modern PUSH so the two channels never diverge.
     * {@code educationType} nullable (both bachelor + master).
     */
    @Query("SELECT s FROM HSpeciality s WHERE s.reviewStatus = uz.hemis.domain.entity.classifier.ReviewStatus.APPROVED " +
           "AND s.code IS NOT NULL AND s.active = true " +
           "AND s.educationType = COALESCE(:educationType, s.educationType) ORDER BY s.code ASC")
    List<HSpeciality> findAllForDistribution(@Param("educationType") String educationType);

    /**
     * SUM(version) over the distributable set — the SAME predicate as {@link #findAllForDistribution}
     * (APPROVED + code-bearing + active). Backs {@code SpecialityClassifierDistResponse.version}: the OTM
     * cache-bust scalar that changes on any curation edit (each edit bumps a row's {@code @Version}); Univer
     * compares it ({@code !=}) to detect a stale classifier. {@code COALESCE(...,0)} keeps an empty set at 0.
     */
    @Query("SELECT COALESCE(SUM(s.version), 0) FROM HSpeciality s " +
           "WHERE s.reviewStatus = uz.hemis.domain.entity.classifier.ReviewStatus.APPROVED " +
           "AND s.code IS NOT NULL AND s.active = true " +
           "AND s.educationType = COALESCE(:educationType, s.educationType)")
    long sumDistributionVersion(@Param("educationType") String educationType);

    // ─── M013 soft delete ────────────────────────────────────────────────────────────────
    // @SQLRestriction("deleted_at IS NULL") filters every JPQL read above (and the inherited
    // findById/findAllById). The two natives below are the deliberate escape hatch: native SQL is
    // NOT rewritten by the restriction, which is exactly why restore/listDeleted can see the rows
    // nothing else may. Same pattern as UniversityDepartmentRepository.findByIdIncludingDeleted.

    /**
     * Find by id INCLUDING soft-deleted rows. {@code restore()} needs it: the class-level
     * restriction makes the inherited {@code findById} blind to precisely the rows restore is about.
     * Every other caller must keep using {@code findById} — a deleted row must 404.
     */
    @Query(value = "SELECT * FROM h_speciality WHERE id = :id", nativeQuery = true)
    Optional<HSpeciality> findByIdIncludingDeleted(@Param("id") UUID id);

    /**
     * Direct children INCLUDING soft-deleted ones — the level-change guard ONLY.
     *
     * <p>A deleted child is restorable and keeps its stored {@code hierarchy_level}, so it must still
     * pin its parent's depth: without this, "delete leaf &rarr; move its parent to another level &rarr;
     * restore the leaf" resurrects the child at a depth that breaks {@code parent.level + 1}, and that
     * broken depth ships to the 224 OTMs inside {@code SpecialityDistItemDto} once the row is
     * promoted. Counting deleted children here keeps {@code restore()} always safe, instead of adding
     * a restore-side guard that could dead-end a row nobody can place any more.</p>
     *
     * <p>The DELETE guard deliberately keeps using {@link #findAllChildren} (live only): deleting a
     * parent whose only children are already deleted creates no inconsistency — restoring the parent
     * first re-establishes a consistent tree, which
     * {@code SPECIALITY_RESTORE_PARENT_DELETED} enforces.</p>
     */
    @Query(value = "SELECT count(*) FROM h_speciality WHERE parent_id = :parentId", nativeQuery = true)
    long countChildrenIncludingDeleted(@Param("parentId") UUID parentId);

    /** Soft-deleted rows, newest first — the "Deleted specialities" list. Scans idx_h_speciality_deleted. */
    @Query(value = "SELECT * FROM h_speciality WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC",
           nativeQuery = true)
    List<HSpeciality> findAllDeleted();

    /**
     * Live holders of the identity key backing {@code uq_h_speciality_identity_live} (M013).
     *
     * <p>JPQL, so {@code @SQLRestriction} supplies {@code deleted_at IS NULL} — it counts exactly
     * what the partial unique index counts. Deliberately NOT filtered on {@code active} (unlike
     * {@link #findExactTwins}): a live-but-inactive twin still owns the slot, and restore must fail
     * with a 422 instead of a raw 23505.</p>
     */
    @Query("SELECT COUNT(s) FROM HSpeciality s WHERE s.educationType = :educationType " +
           "AND s.nameSearch = :nameSearch " +
           "AND ((CAST(:code AS string) IS NULL AND s.code IS NULL) OR s.code = :code)")
    long countLiveIdentity(@Param("educationType") String educationType,
                           @Param("code") String code,
                           @Param("nameSearch") String nameSearch);
}
