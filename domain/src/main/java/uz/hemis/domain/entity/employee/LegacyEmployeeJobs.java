package uz.hemis.domain.entity.employee;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Legacy CUBA mapping for {@code hemishe_e_employee_jobs}.
 *
 * <p>api-legacy modul'da Univer (Yii2 PHP) tomonidan chaqiriladigan
 * {@code POST /entities/hemishe_EEmployeeJobs} endpoint'i shu entity'ga yozadi.
 * Yangi {@link EmployeeJobs} (table: {@code employee_job}) modular schema uchun
 * — ikki entity bir-biriga aralashmaydi (api-legacy/CLAUDE.md "GOLDEN RULE").</p>
 *
 * <p><strong>FK conventions (CUBA legacy):</strong></p>
 * <ul>
 *   <li>{@code _employee} (uuid) → references {@code hemishe_e_teacher.id}</li>
 *   <li>{@code _university} (varchar) → references {@code hemishe_e_university.code}</li>
 *   <li>{@code _department} (varchar) → {@code hemishe_e_university_department.code}</li>
 *   <li>Code field'lar: {@code _employee_type}, {@code _employee_position}, {@code _employee_rate}, {@code _employee_form}, {@code _employee_status}</li>
 * </ul>
 *
 * @since 2.6.0
 */
@Entity
@Table(name = "hemishe_e_employee_jobs")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegacyEmployeeJobs extends BaseEntity {

    @Column(name = "_employee")
    private UUID employeeId;

    @Column(name = "_university", length = 50)
    private String university;

    @Column(name = "_department", length = 50)
    private String department;

    @Column(name = "_employee_type", length = 10)
    private String employeeType;

    @Column(name = "_employee_position", length = 10)
    private String employeePosition;

    @Column(name = "_employee_rate", length = 10)
    private String employeeRate;

    @Column(name = "_employee_form", length = 10)
    private String employeeForm;

    @Column(name = "_employee_status", length = 10)
    private String employeeStatus;

    @Column(name = "job_start_date")
    private LocalDate jobStartDate;

    @Column(name = "job_end_date")
    private LocalDate jobEndDate;

    @Column(name = "tag", length = 100)
    private String tag;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "contract_number", length = 100)
    private String contractNumber;

    @Column(name = "decree_date")
    private LocalDate decreeDate;

    @Column(name = "decree_number", length = 100)
    private String decreeNumber;
}
