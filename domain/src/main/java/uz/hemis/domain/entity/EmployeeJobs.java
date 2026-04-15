package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
@Table(name = "employee_jobs")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeJobs implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "employee_id_number", length = 50)
    private String employeeIdNumber;

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

    @Column(name = "employment_staff", length = 10)
    private String employmentStaff;

    @Column(name = "employee_status", length = 10)
    private String employeeStatus;

    @Column(name = "employee_rate", length = 10)
    private String employeeRate;

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

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "source_uid")
    private String sourceUid;

    @Column(name = "hemis_uid")
    private String hemisUid;

    @Column(name = "hash", length = 64)
    private String hash;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
