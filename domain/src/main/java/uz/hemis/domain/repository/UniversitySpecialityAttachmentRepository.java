package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * EVERY attachment row that points at a speciality — soft-deleted (revoked) ones included.
     *
     * <p>Native on purpose: the entity's {@code @SQLRestriction("deleted_at IS NULL")} hides
     * revoked rows from JPA, but they physically remain and the
     * {@code fk_univ_spec_attach_spec} FK ({@code ON DELETE RESTRICT}) still counts them.
     * The classifier delete guard needs the physical truth so a blocked delete surfaces as a
     * clean 422 instead of a raw constraint-violation 500.</p>
     */
    @Query(value = "SELECT COUNT(*) FROM university_speciality_attachment WHERE speciality_id = :specialityId",
           nativeQuery = true)
    long countAllBySpecialityId(@Param("specialityId") UUID specialityId);

    /**
     * Tenant-scoped paginated search — {@code codes} is the caller's allowed OTM set
     * (always non-empty; a deny-all scope is rejected upstream). Optional
     * {@code specialityId}/{@code status}/{@code educationForm}/{@code educationType} filters.
     *
     * <p>Joined to {@code HSpeciality} (the FK guarantees a match, so the inner join drops nothing)
     * so rows can be ordered by the <strong>hierarchical speciality code</strong> — a direction and
     * its sub-directions share a code prefix, so they come out consecutively (like the classifier
     * tree), in the grid and the streaming export alike. A trailing {@code a.id} keeps a total order.</p>
     */
    @Query(value = "SELECT a FROM UniversitySpecialityAttachment a, HSpeciality s " +
           "WHERE s.id = a.specialityId AND a.universityCode IN :codes " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (CAST(:status AS string) IS NULL OR a.status = :status) " +
           "AND (CAST(:educationForm AS string) IS NULL OR a.educationForm = :educationForm) " +
           "AND (CAST(:educationType AS string) IS NULL OR s.educationType = :educationType) " +
           "AND (:eduYear IS NULL OR a.eduYear = :eduYear) " +
           "ORDER BY a.universityCode ASC, s.code ASC, a.id ASC",
           countQuery = "SELECT COUNT(a) FROM UniversitySpecialityAttachment a, HSpeciality s " +
           "WHERE s.id = a.specialityId AND a.universityCode IN :codes " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (CAST(:status AS string) IS NULL OR a.status = :status) " +
           "AND (CAST(:educationForm AS string) IS NULL OR a.educationForm = :educationForm) " +
           "AND (CAST(:educationType AS string) IS NULL OR s.educationType = :educationType) " +
           "AND (:eduYear IS NULL OR a.eduYear = :eduYear)")
    Page<UniversitySpecialityAttachment> searchScoped(@Param("codes") Collection<String> codes,
                                             @Param("specialityId") UUID specialityId,
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
     * streaming export pages cleanly.
     */
    @Query(value = "SELECT a FROM UniversitySpecialityAttachment a, HSpeciality s " +
           "WHERE s.id = a.specialityId " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (CAST(:status AS string) IS NULL OR a.status = :status) " +
           "AND (CAST(:educationForm AS string) IS NULL OR a.educationForm = :educationForm) " +
           "AND (CAST(:educationType AS string) IS NULL OR s.educationType = :educationType) " +
           "AND (:eduYear IS NULL OR a.eduYear = :eduYear) " +
           "ORDER BY a.universityCode ASC, s.code ASC, a.id ASC",
           countQuery = "SELECT COUNT(a) FROM UniversitySpecialityAttachment a, HSpeciality s " +
           "WHERE s.id = a.specialityId " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (CAST(:status AS string) IS NULL OR a.status = :status) " +
           "AND (CAST(:educationForm AS string) IS NULL OR a.educationForm = :educationForm) " +
           "AND (CAST(:educationType AS string) IS NULL OR s.educationType = :educationType) " +
           "AND (:eduYear IS NULL OR a.eduYear = :eduYear)")
    Page<UniversitySpecialityAttachment> searchAll(@Param("specialityId") UUID specialityId,
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
