package uz.hemis.domain.entity.classifier;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.AuditableEntityNoSoftDelete;

import java.util.UUID;

/**
 * Attach a unified-classifier speciality to an OTM — {@code university_speciality_attachment} (V019).
 *
 * <p>"Which speciality is this university allowed to run."</p>
 *
 * <p><strong>No soft delete</strong> ({@link AuditableEntityNoSoftDelete}): nothing references an
 * attachment, so detaching is a HARD delete — the row is simply re-created when the permission is
 * granted again. A business decision to withdraw the permission while keeping the record is the
 * {@code status} column's job ({@code ACTIVE}/{@code SUSPENDED}/{@code REVOKED}), not a hidden
 * {@code deleted_at} row — so there is no {@code @SQLRestriction} either. Soft delete only ever hurt
 * here: an invisible row still held {@code fk_univ_spec_attach_spec} and blocked the classifier
 * delete with "attached to 3 OTMs" while the registry showed nothing.</p>
 *
 * <p>{@code universityCode} is a by-value reference to
 * {@code hemishe_e_university.code} (the 224-OTM identifier), NOT a UUID FK.
 * {@code specialityId} is a UUID FK into {@link HSpeciality}. Tenant-scope is
 * enforced fail-closed in the service layer.</p>
 *
 * @see AuditableEntityNoSoftDelete
 */
@Entity
@Table(name = "university_speciality_attachment")
@Getter
@Setter
public class UniversitySpecialityAttachment extends AuditableEntityNoSoftDelete {

    private static final long serialVersionUID = 1L;

    @Column(name = "university_code", nullable = false, length = 255)
    private String universityCode;

    @Column(name = "speciality_id", nullable = false)
    private UUID specialityId;

    @Column(name = "education_form", length = 32)
    private String educationForm;

    /** Academic year of THIS assignment (2026 = 2026-2027) — distinct from the speciality's own
     *  validity years in {@code h_speciality_year}. */
    @Column(name = "edu_year", nullable = false)
    private Integer eduYear;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "ACTIVE";
}
