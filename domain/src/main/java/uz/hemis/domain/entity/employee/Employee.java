package uz.hemis.domain.entity.employee;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.common.auth.PersonType;
import uz.hemis.common.vo.PhoneNumber;
import uz.hemis.common.vo.Pinfl;
import uz.hemis.common.vo.Tin;
import uz.hemis.domain.converter.PhoneNumberConverter;
import uz.hemis.domain.converter.PinflConverter;
import uz.hemis.domain.converter.TinConverter;
import uz.hemis.domain.entity.academic.AcademicDegree;
import uz.hemis.domain.entity.academic.AcademicRank;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "employee")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends AuditableEntity {

    @Column(name = "pinfl", nullable = false, unique = true, length = 14)
    @Convert(converter = PinflConverter.class)
    private Pinfl pinfl;

    /**
     * Person type discriminator — decides business context of this employee row.
     *
     * <p>HEMIS stores all physical persons in {@code employee} (universal registry).
     * The discriminator routes them: OTM staff, ministry staff, center staff,
     * or external-org staff (GUVD, Hokimiyat, Tax, …).</p>
     *
     * <p>Default {@code UNIVERSITY_STAFF} — preserves backward-compat with existing
     * 46K rows that have no explicit type.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false, length = 30)
    @Builder.Default
    private PersonType personType = PersonType.UNIVERSITY_STAFF;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    // ==================== Classifier FK fields ====================
    // Dual mapping pattern: String code (write path, backward compat) + LAZY FK (read path, FK-style access).
    // DB FK constraints already declared in V004_create_employee.sql → REFERENCES gender(code), etc.

    // Classifier FK uzunliklari ReferenceEntity.code (VARCHAR(20)) ga mos
    @Column(name = "gender_code", length = 20)
    private String genderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Gender gender;

    @Column(name = "citizenship_code", length = 20)
    private String citizenshipCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizenship_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Citizenship citizenship;

    @Column(name = "nationality_code", length = 20)
    private String nationalityCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Nationality nationality;

    /**
     * Passport identifier — single column for consistency with legacy
     * {@code hemishe_e_employee} and per-OTM university databases.
     *
     * <p>Format: {@code "AA1234567"} (2 letters + 7 digits).
     * OneID / MyGov SSO callbacks that deliver series and number separately must
     * concatenate them at the service layer before persisting.</p>
     *
     * <p>UNIQUE (partial) — no two living employees may share a passport.</p>
     */
    @Column(name = "passport", length = 20)
    private String passport;

    @Column(name = "phone", length = 50)
    @Convert(converter = PhoneNumberConverter.class)
    private PhoneNumber phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "academic_degree_code", length = 20)
    private String academicDegreeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_degree_code", referencedColumnName = "code", insertable = false, updatable = false)
    private AcademicDegree academicDegree;

    @Column(name = "academic_rank_code", length = 20)
    private String academicRankCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_rank_code", referencedColumnName = "code", insertable = false, updatable = false)
    private AcademicRank academicRank;

    @Column(name = "tin", length = 20)
    @Convert(converter = TinConverter.class)
    private Tin tin;

    // NOTE: academic credentials are managed via EmployeeAcademicCredentialRepository
    // directly (idempotent upsert by diploma_number_key). The previous @OneToMany
    // Set + addCredential() helper was orphan code (never invoked) and has been
    // removed.
}
