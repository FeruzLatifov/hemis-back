package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.classifier.UniversitySpecialityAttachment;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository for speciality→OTM attachments ({@code university_speciality_attachment}).
 *
 * <p>Tenant-scope filtering is applied in the service layer (fail-closed); this
 * repository does not enforce it.</p>
 */
@Repository
@Transactional(readOnly = true)
public interface UniversitySpecialityAttachmentRepository extends JpaRepository<UniversitySpecialityAttachment, UUID> {

    List<UniversitySpecialityAttachment> findByUniversityCode(String universityCode);

    List<UniversitySpecialityAttachment> findBySpecialityId(UUID specialityId);

    /**
     * LIVE attachments of a speciality — the ones a user can actually see and act on.
     *
     * <p>The classifier delete guard counts these, NOT every physical row. A revoked
     * (soft-deleted) attachment is already gone as far as the admin is concerned: it is absent
     * from the registry, cannot be opened, cannot be detached again. Blocking a delete on it
     * produced a dead end in production — "attached to 3 OTMs" while the registry showed nothing.
     * The physical rows are purged by {@link #purgeRevokedBySpecialityId} at delete time, which is
     * what actually releases the {@code fk_univ_spec_attach_spec} FK.</p>
     *
     * <p>Still native: the entity's {@code @SQLRestriction} would express the same predicate, but
     * keeping both queries in one language makes the pair auditable side by side.</p>
     */
    @Query(value = "SELECT COUNT(*) FROM university_speciality_attachment " +
           "WHERE speciality_id = :specialityId AND deleted_at IS NULL",
           nativeQuery = true)
    long countLiveBySpecialityId(@Param("specialityId") UUID specialityId);

    /**
     * Physically remove the already-revoked (soft-deleted) attachments of a speciality.
     *
     * <p>Called only from the classifier delete path, after the live-attachment guard passed. These
     * rows carry no meaning any more — the attachment was revoked and the speciality itself is
     * being removed — but {@code ON DELETE RESTRICT} would still block on them. Native because JPA
     * cannot even see rows its {@code @SQLRestriction} filters out.</p>
     *
     * @return how many revoked rows were purged (0 in the normal case)
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM university_speciality_attachment " +
           "WHERE speciality_id = :specialityId AND deleted_at IS NOT NULL",
           nativeQuery = true)
    int purgeRevokedBySpecialityId(@Param("specialityId") UUID specialityId);

    /**
     * The same physical truth as {@link #countAllBySpecialityId}, but GROUPED BY OTM — the list of
     * universities that block a classifier delete, one row each, ordered by OTM code.
     *
     * <p>LIVE rows only — the same predicate as {@link #countLiveBySpecialityId}, so the count in
     * the 422 message and the list in the dialog can never disagree. Grouped rather than
     * row-per-attachment because the admin needs to know <em>where to go</em> (the OTM), not how
     * many form/year rows are there.</p>
     *
     * <p>Returns positional rows — {@code [university_code, count]} — deliberately, NOT an
     * interface projection: a native alias must be double-quoted to keep its case, and an unquoted
     * one silently folds to lower case, leaving a {@code getUniversityCode()} projection returning
     * {@code null} at runtime with nothing failing at compile time. There is no integration test
     * that would catch that here (the domain test profile runs on H2, which does not even parse
     * {@code FILTER (WHERE ...)}), so the mapping is kept independent of alias casing.</p>
     */
    @Query(value = "SELECT university_code, COUNT(*) " +
           "FROM university_speciality_attachment " +
           "WHERE speciality_id = :specialityId AND deleted_at IS NULL " +
           "GROUP BY university_code ORDER BY university_code",
           nativeQuery = true)
    List<Object[]> countLiveBySpecialityIdGroupedByUniversity(@Param("specialityId") UUID specialityId);

    /**
     * Tenant-scoped paginated search — {@code codes} is the caller's allowed OTM set
     * (always non-empty; a deny-all scope is rejected upstream). Optional
     * {@code specialityId}/{@code status}/{@code educationForm}/{@code educationType} filters
     * plus the free-text speciality filter below.
     *
     * <p>Joined to {@code HSpeciality} (the FK guarantees a match, so the inner join drops nothing)
     * so rows can be ordered by the <strong>hierarchical speciality code</strong> — a direction and
     * its sub-directions share a code prefix, so they come out consecutively (like the classifier
     * tree), in the grid and the streaming export alike. A trailing {@code a.id} keeps a total order.</p>
     *
     * <p><strong>Free-text speciality filter</strong> — three binds prepared by the service, all
     * {@code null} together when there is nothing to search for. {@code CAST(:qLike AS string) IS NULL}
     * then short-circuits the whole OR, so an unfiltered list keeps exactly the plan it had before:</p>
     * <ul>
     *   <li>{@code qLike} — {@code %text%} (lower-cased) against the speciality CODE.</li>
     *   <li>{@code qFolded} — {@code %fold(text)%} against {@code s.nameSearch}, the DB-generated
     *       {@code h_speciality_fold(name_uz)} column (V018) — NOT the raw {@code name_uz}. The Uzbek
     *       apostrophe has several live spellings ({@code '}, {@code ʻ}, {@code ’}), so "O'zbek" typed
     *       one way would miss a name stored the other way; the fold maps every variant to a space,
     *       so query and column normalise to the same key. The service folds with the very same
     *       function that seeded the column.</li>
     *   <li>{@code qId} — EXACT speciality id, never a substring: a pasted UUID is an identifier, and a
     *       partial UUID match would be noise rather than a search result.</li>
     * </ul>
     */
    @Query(value = "SELECT a FROM UniversitySpecialityAttachment a, HSpeciality s " +
           "WHERE s.id = a.specialityId AND a.universityCode IN :codes " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (CAST(:qLike AS string) IS NULL OR LOWER(s.code) LIKE :qLike " +
           "OR s.nameSearch LIKE :qFolded OR a.specialityId = :qId) " +
           "AND (CAST(:status AS string) IS NULL OR a.status = :status) " +
           "AND (CAST(:educationForm AS string) IS NULL OR a.educationForm = :educationForm) " +
           "AND (CAST(:educationType AS string) IS NULL OR s.educationType = :educationType) " +
           "AND (:eduYear IS NULL OR a.eduYear = :eduYear) " +
           "ORDER BY a.universityCode ASC, s.code ASC, a.id ASC",
           countQuery = "SELECT COUNT(a) FROM UniversitySpecialityAttachment a, HSpeciality s " +
           "WHERE s.id = a.specialityId AND a.universityCode IN :codes " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (CAST(:qLike AS string) IS NULL OR LOWER(s.code) LIKE :qLike " +
           "OR s.nameSearch LIKE :qFolded OR a.specialityId = :qId) " +
           "AND (CAST(:status AS string) IS NULL OR a.status = :status) " +
           "AND (CAST(:educationForm AS string) IS NULL OR a.educationForm = :educationForm) " +
           "AND (CAST(:educationType AS string) IS NULL OR s.educationType = :educationType) " +
           "AND (:eduYear IS NULL OR a.eduYear = :eduYear)")
    Page<UniversitySpecialityAttachment> searchScoped(@Param("codes") Collection<String> codes,
                                             @Param("specialityId") UUID specialityId,
                                             @Param("qLike") String qLike,
                                             @Param("qFolded") String qFolded,
                                             @Param("qId") UUID qId,
                                             @Param("status") String status,
                                             @Param("educationForm") String educationForm,
                                             @Param("educationType") String educationType,
                                             @Param("eduYear") Integer eduYear,
                                             Pageable pageable);

    /**
     * Unrestricted (ministry/system) paginated search across every OTM — used ONLY when
     * the resolved scope is {@code unrestricted}. Optional filters:
     * {@code specialityId}/{@code status}/{@code educationForm} (attachment columns) and
     * {@code educationType} (resolved via an {@code HSpeciality} subquery — it lives on the
     * classifier, not on the attachment). A trailing {@code a.id} keeps a total order so the
     * streaming export pages cleanly. The free-text {@code qLike}/{@code qFolded}/{@code qId}
     * binds behave exactly as documented on {@link #searchScoped}.
     */
    @Query(value = "SELECT a FROM UniversitySpecialityAttachment a, HSpeciality s " +
           "WHERE s.id = a.specialityId " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (CAST(:qLike AS string) IS NULL OR LOWER(s.code) LIKE :qLike " +
           "OR s.nameSearch LIKE :qFolded OR a.specialityId = :qId) " +
           "AND (CAST(:status AS string) IS NULL OR a.status = :status) " +
           "AND (CAST(:educationForm AS string) IS NULL OR a.educationForm = :educationForm) " +
           "AND (CAST(:educationType AS string) IS NULL OR s.educationType = :educationType) " +
           "AND (:eduYear IS NULL OR a.eduYear = :eduYear) " +
           "ORDER BY a.universityCode ASC, s.code ASC, a.id ASC",
           countQuery = "SELECT COUNT(a) FROM UniversitySpecialityAttachment a, HSpeciality s " +
           "WHERE s.id = a.specialityId " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (CAST(:qLike AS string) IS NULL OR LOWER(s.code) LIKE :qLike " +
           "OR s.nameSearch LIKE :qFolded OR a.specialityId = :qId) " +
           "AND (CAST(:status AS string) IS NULL OR a.status = :status) " +
           "AND (CAST(:educationForm AS string) IS NULL OR a.educationForm = :educationForm) " +
           "AND (CAST(:educationType AS string) IS NULL OR s.educationType = :educationType) " +
           "AND (:eduYear IS NULL OR a.eduYear = :eduYear)")
    Page<UniversitySpecialityAttachment> searchAll(@Param("specialityId") UUID specialityId,
                                          @Param("qLike") String qLike,
                                          @Param("qFolded") String qFolded,
                                          @Param("qId") UUID qId,
                                          @Param("status") String status,
                                          @Param("educationForm") String educationForm,
                                          @Param("educationType") String educationType,
                                          @Param("eduYear") Integer eduYear,
                                          Pageable pageable);

    // =====================================================
    // Filter-option sources — only values that ACTUALLY occur in attachments (not the full
    // classifier), so the UI dropdowns never offer a choice that yields an empty result.
    // =====================================================

    /** Distinct OTM codes that have at least one (live) attachment. */
    @Query("SELECT DISTINCT a.universityCode FROM UniversitySpecialityAttachment a ORDER BY a.universityCode")
    List<String> findDistinctUniversityCodes();

    /** Distinct education-form codes present in attachments (non-null). */
    @Query("SELECT DISTINCT a.educationForm FROM UniversitySpecialityAttachment a " +
           "WHERE a.educationForm IS NOT NULL ORDER BY a.educationForm")
    List<String> findDistinctEducationForms();

    /** Distinct education-type codes present in attachments (resolved via the speciality classifier). */
    @Query("SELECT DISTINCT s.educationType FROM HSpeciality s WHERE s.educationType IS NOT NULL " +
           "AND s.id IN (SELECT a.specialityId FROM UniversitySpecialityAttachment a) ORDER BY s.educationType")
    List<String> findDistinctEducationTypes();

    /** Distinct academic years present in attachments, newest first (grows as future years are seeded). */
    @Query("SELECT DISTINCT a.eduYear FROM UniversitySpecialityAttachment a ORDER BY a.eduYear DESC")
    List<Integer> findDistinctEduYears();

    /**
     * Duplicate guard: a live attachment for the same (OTM, speciality, education_form, edu_year),
     * excluding the row being updated. {@code @SQLRestriction} already excludes soft-deleted rows.
     */
    @Query("SELECT COUNT(a) > 0 FROM UniversitySpecialityAttachment a " +
           "WHERE a.universityCode = :universityCode AND a.specialityId = :specialityId " +
           "AND a.eduYear = :eduYear AND a.educationForm = :educationForm " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId)")
    boolean existsDuplicate(@Param("universityCode") String universityCode,
                            @Param("specialityId") UUID specialityId,
                            @Param("educationForm") String educationForm,
                            @Param("eduYear") int eduYear,
                            @Param("excludeId") UUID excludeId);
}
