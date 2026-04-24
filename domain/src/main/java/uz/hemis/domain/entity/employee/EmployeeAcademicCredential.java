package uz.hemis.domain.entity.employee;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import uz.hemis.common.enums.CredentialType;
import uz.hemis.domain.entity.academic.AcademicDegree;
import uz.hemis.domain.entity.academic.AcademicRank;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One awarded academic credential for an employee — either a {@code DEGREE}
 * (ilmiy daraja — PhD, DSc, Fan nomzodi) or a {@code TITLE} (ilmiy unvon —
 * Doцent, Professor). Populated by the SAC (Science Academic Center) API.
 *
 * <p><strong>Pattern:</strong> Single Table Inheritance (Martin Fowler PEAA).
 * One table, one row per credential, discriminator column chooses which
 * classifier FK is populated:</p>
 *
 * <ul>
 *   <li>{@link CredentialType#DEGREE} → {@link #degree} (hemishe_h_academic_degree)</li>
 *   <li>{@link CredentialType#TITLE}  → {@link #rank}   (hemishe_h_academic_rank)</li>
 * </ul>
 *
 * <p>Per-type invariants are enforced by DB {@code CHECK} constraint —
 * see {@code chk_eac_xor} in {@code V004_create_employee.sql}.</p>
 *
 * <p>Idempotent upsert key: {@code (employee_id, diploma_number)}.</p>
 *
 * @since 2.1.0
 */
@Entity
@Table(name = "employee_academic_credential")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class EmployeeAcademicCredential extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** STI discriminator — DEGREE or TITLE. */
    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false, length = 10)
    private CredentialType credentialType;

    // =====================================================
    // Classifier FK'lar (XOR — enforced via DB CHECK constraint)
    // =====================================================

    /** Degree classifier (hemishe_h_academic_degree). NULL for TITLE rows. */
    @Column(name = "degree_code", length = 20)
    private String degreeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_code", referencedColumnName = "code",
                insertable = false, updatable = false)
    private AcademicDegree degree;

    /** Rank classifier (hemishe_h_academic_rank). NULL for DEGREE rows. */
    @Column(name = "rank_code", length = 20)
    private String rankCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_code", referencedColumnName = "code",
                insertable = false, updatable = false)
    private AcademicRank rank;

    // =====================================================
    // External SAC API identifiers (for re-sync / audit)
    // =====================================================

    /**
     * SAC API integer — {@code title_code} (for TITLE) or {@code degree_code} (for DEGREE).
     *
     * <p>Stored verbatim. SAC is the authoritative source; our legacy classifiers
     * lag behind. FK columns above are populated when mapping exists; these raw
     * fields are populated on every sync.</p>
     */
    @Column(name = "external_classifier_code")
    private Integer externalClassifierCode;

    /**
     * SAC API text — {@code title} (TITLE) or {@code degree_name} (DEGREE).
     * Used as display fallback when the classifier FK is NULL (unmapped).
     */
    @Column(name = "external_classifier_name", length = 255)
    private String externalClassifierName;

    /** SAC API integer — {@code science_sector_code}. */
    @Column(name = "external_sector_code")
    private Integer externalSectorCode;

    /**
     * SAC API text — {@code science_sector} ("Техника фанлари", …).
     * Display fallback when {@code science_branch_code} FK is NULL.
     */
    @Column(name = "external_sector_name", length = 255)
    private String externalSectorName;

    // =====================================================
    // Shared fields
    // =====================================================

    @Column(name = "speciality", length = 500)
    private String speciality;

    @Column(name = "diploma_number", nullable = false, length = 100)
    private String diplomaNumber;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    // =====================================================
    // DEGREE-only
    // =====================================================

    /** Dissertation theme — populated only when {@code credentialType == DEGREE}. */
    @Column(name = "theme", columnDefinition = "TEXT")
    private String theme;

    // =====================================================
    // Source tracking (SAC API audit)
    // =====================================================

    @Column(name = "source", nullable = false, length = 50)
    private String source = "sac-api";

    /** Original API response record (JSONB) — full audit trail. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_raw", columnDefinition = "jsonb")
    private String sourceRaw;

    @Column(name = "source_updated_at")
    private LocalDateTime sourceUpdatedAt;

    // =====================================================
    // Business methods
    // =====================================================

    public boolean isDegree() {
        return credentialType == CredentialType.DEGREE;
    }

    public boolean isTitle() {
        return credentialType == CredentialType.TITLE;
    }

    /**
     * Factory enforcing the rule: SAC sync never creates new employees — it only
     * attaches credentials to already-persisted ones (whose primary source is
     * MyGov / OneID / university HR).
     *
     * <p>Service layer (SAC sync) must resolve {@code Employee} by PINFL first
     * and bail out with a log warning if absent, THEN call this factory. An
     * unpersisted employee triggers {@link IllegalStateException} — fails fast
     * in tests and at runtime.</p>
     *
     * @param employee      already-persisted Employee (id must be non-null)
     * @param type          DEGREE or TITLE
     * @param diplomaNumber raw diploma identifier (DB normalizes for uniqueness)
     */
    public static EmployeeAcademicCredential forExisting(Employee employee,
                                                         CredentialType type,
                                                         String diplomaNumber) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalStateException(
                    "SAC credentials may only be attached to an already-persisted employee. " +
                    "Resolve by PINFL first (MyGov/OneID/HR sync owns employee lifecycle).");
        }
        if (type == null) {
            throw new IllegalArgumentException("credentialType is required");
        }
        if (diplomaNumber == null || diplomaNumber.isBlank()) {
            throw new IllegalArgumentException("diplomaNumber is required (UPSERT key)");
        }
        EmployeeAcademicCredential c = new EmployeeAcademicCredential();
        c.setEmployee(employee);
        c.setCredentialType(type);
        c.setDiplomaNumber(diplomaNumber);
        return c;
    }
}
