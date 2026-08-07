package uz.hemis.domain.entity.classifier;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.util.UUID;

/**
 * Attach a unified-classifier speciality to an OTM — {@code h_speciality_attachment} (V019).
 *
 * <p>"Which speciality is this university allowed to run." A revocable business
 * record, so it uses {@link AuditableEntity} (modern audit WITH soft delete via
 * {@code deleted_at}), unlike the classifier rows themselves.</p>
 *
 * <p>{@code universityCode} is a by-value reference to
 * {@code hemishe_e_university.code} (the 224-OTM identifier), NOT a UUID FK.
 * {@code specialityId} is a UUID FK into {@link HSpeciality}. Tenant-scope is
 * enforced fail-closed in the service layer.</p>
 */
@Entity
@Table(name = "h_speciality_attachment")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class HSpecialityAttachment extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "university_code", nullable = false, length = 255)
    private String universityCode;

    @Column(name = "speciality_id", nullable = false)
    private UUID specialityId;

    @Column(name = "education_form", length = 32)
    private String educationForm;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "ACTIVE";
}
