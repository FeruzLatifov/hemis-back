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
 * Grade Entity - Mapped to hemishe_e_grade table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_grade</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Represents student grade/mark for a course.</p>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_grade")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Grade extends BaseEntity {

    private static final long serialVersionUID = 1L;

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
    // Course Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Course ID
     * Column: _course UUID
     * References: hemishe_e_course.id
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_course")
    private UUID course;

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
    // Teacher Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Teacher ID
     * Column: _teacher UUID
     * References: hemishe_e_teacher.id
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_teacher")
    private UUID teacher;

    // =====================================================
    // Grade Fields
    // =====================================================

    /**
     * Grade value (numeric)
     * Column: grade_value INTEGER
     * Examples: 5, 4, 3, 2 (or 100-point scale)
     */
    @Column(name = "grade_value")
    private Integer gradeValue;

    /**
     * Grade letter
     * Column: grade_letter VARCHAR(10)
     * Examples: 'A', 'B', 'C', 'D', 'F'
     */
    @Column(name = "grade_letter", length = 10)
    private String gradeLetter;

    /**
     * Grade points (GPA)
     * Column: grade_points DECIMAL(3,2)
     * Examples: 4.00, 3.67, 3.33
     */
    @Column(name = "grade_points")
    private Double gradePoints;

    /**
     * Grade date
     * Column: grade_date DATE
     */
    @Column(name = "grade_date")
    private LocalDate gradeDate;

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
     * Semester
     * Column: semester INTEGER
     */
    @Column(name = "semester")
    private Integer semester;

    /**
     * Attempt number
     * Column: attempt_number INTEGER
     * Default: 1 (first attempt)
     */
    @Column(name = "attempt_number")
    private Integer attemptNumber;

    // =====================================================
    // Classifiers (LEGACY FIELDS WITH _)
    // =====================================================

    /**
     * Assessment type code
     * Column: _assessment_type VARCHAR(32)
     * References: hemishe_h_assessment_type.code
     * Examples: 'EXAM', 'CREDIT', 'DIFF_CREDIT', 'COURSEWORK'
     */
    @Column(name = "_assessment_type", length = 32)
    private String assessmentType;

    /**
     * Grade type code
     * Column: _grade_type VARCHAR(32)
     * References: hemishe_h_grade_type.code
     * Examples: 'FINAL', 'MIDTERM', 'QUIZ', 'ASSIGNMENT'
     */
    @Column(name = "_grade_type", length = 32)
    private String gradeType;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Passed flag
     * Column: is_passed BOOLEAN
     */
    @Column(name = "is_passed")
    private Boolean isPassed;

    /**
     * Finalized flag (cannot be changed)
     * Column: is_finalized BOOLEAN
     */
    @Column(name = "is_finalized")
    private Boolean isFinalized;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if student passed
     *
     * @return true if isPassed flag is true
     */
    public boolean isPassed() {
        return Boolean.TRUE.equals(isPassed);
    }

    /**
     * Check if grade is finalized
     *
     * @return true if isFinalized flag is true
     */
    public boolean isFinalized() {
        return Boolean.TRUE.equals(isFinalized);
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + getId() +
                ", student=" + student +
                ", course=" + course +
                ", gradeValue=" + gradeValue +
                ", gradeLetter='" + gradeLetter + '\'' +
                ", gradePoints=" + gradePoints +
                ", isPassed=" + isPassed +
                '}';
    }
}
