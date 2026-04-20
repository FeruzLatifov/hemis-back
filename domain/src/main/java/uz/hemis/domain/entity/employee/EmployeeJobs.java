package uz.hemis.domain.entity.employee;

import uz.hemis.domain.entity.academic.Specialty;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.time.LocalDate;

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

    @Column(name = "department_code")
    private String departmentCode;

    @Column(name = "position_code", length = 10)
    private String positionCode;

    @Column(name = "employee_type", length = 10)
    private String employeeType;

    @Column(name = "employment_form", length = 10)
    private String employmentForm;

    @Column(name = "employee_rate", length = 10)
    private String employeeRate;

    /** Specialty for this job (assignment-scoped, not person-scoped). */
    @Column(name = "specialty", length = 500)
    private String specialty;

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
}
