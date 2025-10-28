package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * Curriculum Entity - Mapped to hemishe_e_curriculum table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_curriculum</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Represents an academic program curriculum for a specialty.</p>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_curriculum")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Curriculum extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Curriculum code (unique within university)
     * Column: code VARCHAR(255)
     */
    @Column(name = "code", length = 255)
    private String code;

    /**
     * Curriculum name
     * Column: name VARCHAR(1024)
     */
    @Column(name = "name", length = 1024)
    private String name;

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
    // Academic Fields
    // =====================================================

    /**
     * Academic year
     * Column: academic_year VARCHAR(32)
     * Format: "2024/2025"
     */
    @Column(name = "academic_year", length = 32)
    private String academicYear;

    /**
     * Total credits
     * Column: total_credits INTEGER
     */
    @Column(name = "total_credits")
    private Integer totalCredits;

    /**
     * Study duration in years
     * Column: study_duration INTEGER
     */
    @Column(name = "study_duration")
    private Integer studyDuration;

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
     * Curriculum type code
     * Column: _curriculum_type VARCHAR(32)
     * References: hemishe_h_curriculum_type.code
     */
    @Column(name = "_curriculum_type", length = 32)
    private String curriculumType;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Active flag
     * Column: active BOOLEAN
     */
    @Column(name = "active")
    private Boolean active;

    /**
     * Approved flag
     * Column: is_approved BOOLEAN
     */
    @Column(name = "is_approved")
    private Boolean isApproved;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if curriculum is active
     *
     * @return true if active flag is true AND not deleted
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    /**
     * Check if curriculum is approved
     *
     * @return true if isApproved flag is true
     */
    public boolean isApproved() {
        return Boolean.TRUE.equals(isApproved);
    }

    @Override
    public String toString() {
        return "Curriculum{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", university='" + university + '\'' +
                ", specialty=" + specialty +
                ", academicYear='" + academicYear + '\'' +
                ", active=" + active +
                '}';
    }
}
