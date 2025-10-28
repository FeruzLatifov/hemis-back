package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Enrollment Entity - Mapped to hemishe_e_enrollment table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_enrollment</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Represents student enrollment in a specialty/program.</p>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_enrollment")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Enrollment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Enrollment number (unique)
     * Column: enrollment_number VARCHAR(255)
     */
    @Column(name = "enrollment_number", length = 255)
    private String enrollmentNumber;

    // =====================================================
    // Student Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Student ID
     * Column: _student UUID
     * References: hemishe_e_student.id
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_student")
    private UUID student;

    // =====================================================
    // University Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * University code
     * Column: _university VARCHAR(255)
     * References: hemishe_e_university.code (VARCHAR PK)
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_university", length = 255)
    private String university;

    // =====================================================
    // Specialty Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Specialty ID
     * Column: _specialty UUID
     * References: hemishe_e_specialty.id
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_specialty")
    private UUID specialty;

    // =====================================================
    // Faculty Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Faculty ID
     * Column: _faculty UUID
     * References: hemishe_e_faculty.id
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_faculty")
    private UUID faculty;

    // =====================================================
    // Academic Fields
    // =====================================================

    /**
     * Enrollment date
     * Column: enrollment_date DATE
     */
    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    /**
     * Academic year
     * Column: academic_year VARCHAR(32)
     * Format: "2024/2025"
     */
    @Column(name = "academic_year", length = 32)
    private String academicYear;

    /**
     * Course (year of study)
     * Column: course INTEGER
     * Values: 1, 2, 3, 4
     */
    @Column(name = "course")
    private Integer course;

    // =====================================================
    // Classifiers (LEGACY FIELDS WITH _)
    // =====================================================

    /**
     * Education type code
     * Column: _education_type VARCHAR(32)
     * References: hemishe_h_education_type.code
     * Examples: '11' = Bachelor, '12' = Master, '13' = PhD
     */
    @Column(name = "_education_type", length = 32)
    private String educationType;

    /**
     * Education form code
     * Column: _education_form VARCHAR(32)
     * References: hemishe_h_education_form.code
     * Examples: '11' = Full-time, '12' = Part-time, '13' = Evening, '14' = Distance
     */
    @Column(name = "_education_form", length = 32)
    private String educationForm;

    /**
     * Payment form code
     * Column: _payment_form VARCHAR(32)
     * References: hemishe_h_payment_form.code
     * Examples: 'BUDGET', 'CONTRACT'
     */
    @Column(name = "_payment_form", length = 32)
    private String paymentForm;

    /**
     * Enrollment status code
     * Column: _enrollment_status VARCHAR(32)
     * References: hemishe_h_enrollment_status.code
     * Examples: 'ACTIVE', 'ACADEMIC_LEAVE', 'EXPELLED', 'GRADUATED'
     */
    @Column(name = "_enrollment_status", length = 32)
    private String enrollmentStatus;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Active flag
     * Column: active BOOLEAN
     */
    @Column(name = "active")
    private Boolean active;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if enrollment is active
     *
     * @return true if active flag is true AND not deleted
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + getId() +
                ", enrollmentNumber='" + enrollmentNumber + '\'' +
                ", student=" + student +
                ", university='" + university + '\'' +
                ", specialty=" + specialty +
                ", academicYear='" + academicYear + '\'' +
                ", active=" + active +
                '}';
    }
}
