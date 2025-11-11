package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Employment Entity
 *
 * Table: hemishe_e_employment
 * Purpose: Graduate employment tracking
 *
 * Relationships:
 * - Belongs to Student (graduated)
 * - Belongs to University
 * - References Employer/Company
 *
 * Legacy: Maps to old-HEMIS EmploymentService
 *
 * Business Rules:
 * - Only for graduated students (student_status = '16')
 * - Employment status tracked for ministry reporting
 * - Required for university accreditation metrics
 */
@Entity
@Table(name = "hemishe_e_employment")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "employment_code", length = 64, unique = true)
    private String employmentCode;

    @Column(name = "_student")
    private UUID student;

    @Column(name = "_university", length = 64)
    private String university;

    @Column(name = "_diploma")
    private UUID diploma;

    @Column(name = "company_name", length = 512)
    private String companyName;

    @Column(name = "company_tin", length = 32)
    private String companyTin;

    @Column(name = "company_address", length = 512)
    private String companyAddress;

    @Column(name = "company_phone", length = 32)
    private String companyPhone;

    @Column(name = "_employment_type", length = 32)
    private String employmentType;

    @Column(name = "position", length = 256)
    private String position;

    @Column(name = "employment_date")
    private LocalDate employmentDate;

    @Column(name = "contract_number", length = 128)
    private String contractNumber;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "salary", precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(name = "_employment_status", length = 32)
    private String employmentStatus;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "termination_reason", length = 512)
    private String terminationReason;

    @Column(name = "_soato", length = 20)
    private String soato;

    @Column(name = "_industry_code", length = 32)
    private String industryCode;

    @Column(name = "is_specialty_related")
    private Boolean isSpecialtyRelated;

    @Column(name = "notes", length = 2048)
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive;

    public boolean isCurrentlyEmployed() {
        return "ACTIVE".equals(employmentStatus) && terminationDate == null && Boolean.TRUE.equals(isActive);
    }
}
