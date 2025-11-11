package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * Group Entity - Mapped to hemishe_e_group table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_group</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Represents student academic group.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_group")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Group extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Group name/code
     * Column: name VARCHAR(255)
     * Example: "CS-101", "Math-201"
     */
    @Column(name = "name", length = 255)
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
    // Curriculum Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Curriculum ID
     * Column: _curriculum UUID
     * References: hemishe_e_curriculum.id
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_curriculum")
    private UUID curriculum;

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
     * Course (year of study)
     * Column: course INTEGER
     * Values: 1, 2, 3, 4
     */
    @Column(name = "course")
    private Integer course;

    /**
     * Student capacity
     * Column: capacity INTEGER
     */
    @Column(name = "capacity")
    private Integer capacity;

    /**
     * Current student count
     * Column: student_count INTEGER
     */
    @Column(name = "student_count")
    private Integer studentCount;

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
     * Examples: '11' = Full-time, '12' = Part-time
     */
    @Column(name = "_education_form", length = 32)
    private String educationForm;

    /**
     * Education language code
     * Column: _education_lang VARCHAR(32)
     * References: hemishe_h_language.code
     * Examples: 'UZ', 'RU', 'EN'
     */
    @Column(name = "_education_lang", length = 32)
    private String educationLang;

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
     * Check if group is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    /**
     * Check if group is full
     */
    public boolean isFull() {
        return capacity != null && studentCount != null && studentCount >= capacity;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", university='" + university + '\'' +
                ", specialty=" + specialty +
                ", academicYear='" + academicYear + '\'' +
                ", course=" + course +
                ", active=" + active +
                '}';
    }
}
