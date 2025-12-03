package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StudentMeta Entity - Mapped to hemishe_e_student_meta table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_student_meta (EXACT name from ministry.sql)</li>
 *   <li>Columns: All names preserved with underscores</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 *   <li>41 columns total (7 audit + 34 business)</li>
 *   <li>Unique index: (u_id, _university)</li>
 * </ul>
 *
 * <p><strong>NO RENAME • NO DELETE • NO BREAKING CHANGES</strong></p>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_student_meta")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class StudentMeta extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Business Keys
    // =====================================================

    /**
     * University-specific unique ID
     * Column: u_id INTEGER
     * Part of unique index: (u_id, _university)
     */
    @Column(name = "u_id")
    private Integer uId;

    /**
     * University code
     * Column: _university VARCHAR(255)
     * References: hemishe_e_university.code
     * Part of unique index: (u_id, _university)
     */
    @Column(name = "_university", length = 255)
    private String university;

    /**
     * Student ID number (university internal)
     * Column: _stdent_id_number VARCHAR(255)
     * Note: Typo "stdent" is in original schema
     */
    @Column(name = "_stdent_id_number", length = 255)
    private String studentIdNumber;

    // =====================================================
    // References (UUID)
    // =====================================================

    /**
     * Student UUID reference
     * Column: _student UUID
     * References: hemishe_e_student.id
     */
    @Column(name = "_student")
    private UUID student;

    /**
     * Department UUID reference
     * Column: _department UUID
     * References: hemishe_e_university_department.id
     */
    @Column(name = "_department")
    private UUID department;

    // =====================================================
    // Education Classifiers (VARCHAR codes)
    // =====================================================

    /**
     * Education type code
     * Column: _education_type VARCHAR(32)
     * References: hemishe_h_education_type.code
     * Examples: '11' = bachelor, '12' = master
     */
    @Column(name = "_education_type", length = 32)
    private String educationType;

    /**
     * Education form code
     * Column: _education_form VARCHAR(32)
     * References: hemishe_h_education_form.code
     * Examples: '11' = full-time, '12' = part-time
     */
    @Column(name = "_education_form", length = 32)
    private String educationForm;

    /**
     * Semester code
     * Column: _semester VARCHAR(32)
     */
    @Column(name = "_semester", length = 32)
    private String semester;

    /**
     * Level code (course level)
     * Column: _level VARCHAR(32)
     */
    @Column(name = "_level", length = 32)
    private String level;

    /**
     * Education year code
     * Column: _education_year VARCHAR(32)
     * References: hemishe_h_education_year.code
     * Examples: '2023', '2024'
     */
    @Column(name = "_education_year", length = 32)
    private String educationYear;

    /**
     * Payment form code
     * Column: _payment_form VARCHAR(32)
     * References: hemishe_h_payment_form.code
     * Examples: '11' = budget, '12' = contract
     */
    @Column(name = "_payment_form", length = 32)
    private String paymentForm;

    /**
     * Student status code
     * Column: _student_status VARCHAR(32)
     * References: hemishe_h_student_status.code
     * Examples: '11' = active, '16' = graduated
     */
    @Column(name = "_student_status", length = 32)
    private String studentStatus;

    // =====================================================
    // Group Information
    // =====================================================

    /**
     * Group ID (integer)
     * Column: group_id INTEGER
     */
    @Column(name = "group_id")
    private Integer groupId;

    /**
     * Group name
     * Column: group_name VARCHAR(255)
     */
    @Column(name = "group_name", length = 255)
    private String groupName;

    /**
     * Subgroup ID
     * Column: subgroup_id INTEGER
     */
    @Column(name = "subgroup_id")
    private Integer subgroupId;

    /**
     * Subgroup name
     * Column: subgroup_name VARCHAR(255)
     */
    @Column(name = "subgroup_name", length = 255)
    private String subgroupName;

    // =====================================================
    // Registration Information
    // =====================================================

    /**
     * Diploma registration status
     * Column: diploma_registration INTEGER
     */
    @Column(name = "diploma_registration")
    private Integer diplomaRegistration;

    /**
     * Employment registration status
     * Column: employment_registration INTEGER
     */
    @Column(name = "employment_registration")
    private Integer employmentRegistration;

    // =====================================================
    // Order Information
    // =====================================================

    /**
     * Order number
     * Column: order_number VARCHAR(255)
     */
    @Column(name = "order_number", length = 255)
    private String orderNumber;

    /**
     * Order date
     * Column: order_date DATE
     */
    @Column(name = "order_date")
    private LocalDate orderDate;

    /**
     * Status change reason
     * Column: _status_change_reason VARCHAR(1024)
     */
    @Column(name = "_status_change_reason", length = 1024)
    private String statusChangeReason;

    // =====================================================
    // Speciality Information
    // =====================================================

    /**
     * Speciality code
     * Column: speciality VARCHAR(255)
     */
    @Column(name = "speciality", length = 255)
    private String speciality;

    /**
     * Accreditation accepted flag
     * Column: accreditation_accepted BOOLEAN
     */
    @Column(name = "accreditation_accepted")
    private Boolean accreditationAccepted;

    // =====================================================
    // Decree Information
    // =====================================================

    /**
     * Decree number
     * Column: decree_number VARCHAR(255)
     */
    @Column(name = "decree_number", length = 255)
    private String decreeNumber;

    /**
     * Decree name
     * Column: decree_name VARCHAR(1024)
     */
    @Column(name = "decree_name", length = 1024)
    private String decreeName;

    /**
     * Decree date
     * Column: decree_date DATE
     */
    @Column(name = "decree_date")
    private LocalDate decreeDate;

    // =====================================================
    // Additional Classifiers
    // =====================================================

    /**
     * Academic mobile code
     * Column: _academic_mobile VARCHAR(32)
     */
    @Column(name = "_academic_mobile", length = 32)
    private String academicMobile;

    /**
     * Grant type code
     * Column: _grant_type VARCHAR(32)
     */
    @Column(name = "_grant_type", length = 32)
    private String grantType;

    /**
     * Student data contract ID
     * Column: _student_data_contract INTEGER
     */
    @Column(name = "_student_data_contract")
    private Integer studentDataContract;

    /**
     * Restore meta ID (for restoration tracking)
     * Column: _restore_meta_id INTEGER
     */
    @Column(name = "_restore_meta_id")
    private Integer restoreMetaId;

    // =====================================================
    // Status Fields
    // =====================================================

    /**
     * Active flag
     * Column: active BOOLEAN
     */
    @Column(name = "active")
    private Boolean active;

    // =====================================================
    // University Timestamps
    // =====================================================

    /**
     * University created at timestamp
     * Column: university_created_at TIMESTAMP
     */
    @Column(name = "university_created_at")
    private LocalDateTime universityCreatedAt;

    /**
     * University updated at timestamp
     * Column: university_updated_at TIMESTAMP
     */
    @Column(name = "university_updated_at")
    private LocalDateTime universityUpdatedAt;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if student meta is active
     *
     * @return true if active flag is true AND not deleted
     */
    public boolean isActiveMeta() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }
}
