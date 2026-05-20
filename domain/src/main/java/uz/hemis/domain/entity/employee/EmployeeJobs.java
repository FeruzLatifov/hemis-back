package uz.hemis.domain.entity.employee;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.university.UniversityDepartment;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dual mapping pattern: String code + LAZY FK (insertable=false, updatable=false).
 * Write via *Code setter (backward compat), read via FK getter (relational access).
 */

/**
 * EmployeeJobs — one person can hold many positions at many universities.
 *
 * <p>Direct successor of {@code hemishe_e_employee_jobs} (old CUBA table).</p>
 *
 * <p>World equivalents:
 * <ul>
 *   <li>PeopleSoft: {@code JOB} / {@code PS_JOB}</li>
 *   <li>Oracle HCM: {@code PER_ALL_ASSIGNMENTS_F}</li>
 *   <li>Banner: {@code NBRJOBS}</li>
 * </ul></p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "employee_job")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeJobs extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "university_code", nullable = false)
    private String universityCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_code", referencedColumnName = "code", insertable = false, updatable = false)
    private University university;

    @Column(name = "department_code")
    private String departmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_code", referencedColumnName = "code", insertable = false, updatable = false)
    private UniversityDepartment department;

    @Column(name = "position_code", length = 10)
    private String positionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_code", referencedColumnName = "code", insertable = false, updatable = false)
    private HPosition position;

    @Column(name = "position_type_code", length = 10)
    private String positionTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_type_code", referencedColumnName = "code", insertable = false, updatable = false)
    private HPositionType positionType;

    @Column(name = "employment_form_code", length = 10)
    private String employmentFormCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_form_code", referencedColumnName = "code", insertable = false, updatable = false)
    private EmploymentForm employmentForm;

    @Column(name = "employee_rate_code", length = 10)
    private String employeeRateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_rate_code", referencedColumnName = "code", insertable = false, updatable = false)
    private EmployeeRate employeeRate;

    @Builder.Default
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "contract_number", length = 100)
    private String contractNumber;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "decree_number", length = 100)
    private String decreeNumber;

    @Column(name = "decree_date")
    private LocalDate decreeDate;

    /** V014 sync — Univer'ning ichki ID. Idempotent upsert key bilan (university_code, source_uid). */
    @Column(name = "source_uid", length = 100)
    private String sourceUid;

    /** V014 sync — oxirgi sync vaqti (NULL = manual yaratilgan). */
    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
