package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.classifier.HSpecialityAttachment;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository for speciality→OTM attachments ({@code h_speciality_attachment}).
 *
 * <p>Tenant-scope filtering is applied in the service layer (fail-closed); this
 * repository does not enforce it.</p>
 */
@Repository
@Transactional(readOnly = true)
public interface HSpecialityAttachmentRepository extends JpaRepository<HSpecialityAttachment, UUID> {

    List<HSpecialityAttachment> findByUniversityCode(String universityCode);

    List<HSpecialityAttachment> findBySpecialityId(UUID specialityId);

    /**
     * Tenant-scoped paginated search — {@code codes} is the caller's allowed OTM set
     * (always non-empty; a deny-all scope is rejected upstream). Optional
     * {@code specialityId}/{@code status} filters.
     */
    @Query("SELECT a FROM HSpecialityAttachment a WHERE a.universityCode IN :codes " +
           "AND (:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.universityCode ASC")
    Page<HSpecialityAttachment> searchScoped(@Param("codes") Collection<String> codes,
                                             @Param("specialityId") UUID specialityId,
                                             @Param("status") String status,
                                             Pageable pageable);

    /**
     * Unrestricted (ministry/system) paginated search across every OTM — used ONLY when
     * the resolved scope is {@code unrestricted}. Optional {@code specialityId}/{@code status} filters.
     */
    @Query("SELECT a FROM HSpecialityAttachment a WHERE " +
           "(:specialityId IS NULL OR a.specialityId = :specialityId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.universityCode ASC")
    Page<HSpecialityAttachment> searchAll(@Param("specialityId") UUID specialityId,
                                          @Param("status") String status,
                                          Pageable pageable);

    /**
     * Duplicate guard: a live attachment for the same (OTM, speciality, education_form),
     * excluding the row being updated. {@code @SQLRestriction} already excludes soft-deleted rows.
     */
    @Query("SELECT COUNT(a) > 0 FROM HSpecialityAttachment a " +
           "WHERE a.universityCode = :universityCode AND a.specialityId = :specialityId " +
           "AND ((:educationForm IS NULL AND a.educationForm IS NULL) OR a.educationForm = :educationForm) " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId)")
    boolean existsDuplicate(@Param("universityCode") String universityCode,
                            @Param("specialityId") UUID specialityId,
                            @Param("educationForm") String educationForm,
                            @Param("excludeId") UUID excludeId);
}
