package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * Course Entity - Mapped to hemishe_e_course table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_course</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_course")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Course extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Course code (unique within university)
     * Column: code VARCHAR(255)
     */
    @Column(name = "code", length = 255)
    private String code;

    /**
     * Course name
     * Column: name VARCHAR(1024)
     */
    @Column(name = "name", length = 1024)
    private String name;

    /**
     * Short name
     * Column: short_name VARCHAR(255)
     */
    @Column(name = "short_name", length = 255)
    private String shortName;

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
    // Subject Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Subject ID
     * Column: _subject UUID
     * References: hemishe_h_subject.id
     *
     * CRITICAL: Underscore prefix preserved from legacy schema
     */
    @Column(name = "_subject")
    private UUID subject;

    // =====================================================
    // Academic Fields
    // =====================================================

    /**
     * Credit count
     * Column: credit_count INTEGER
     */
    @Column(name = "credit_count")
    private Integer creditCount;

    /**
     * Total hours
     * Column: total_hours INTEGER
     */
    @Column(name = "total_hours")
    private Integer totalHours;

    /**
     * Lecture hours
     * Column: lecture_hours INTEGER
     */
    @Column(name = "lecture_hours")
    private Integer lectureHours;

    /**
     * Practice hours
     * Column: practice_hours INTEGER
     */
    @Column(name = "practice_hours")
    private Integer practiceHours;

    /**
     * Lab hours
     * Column: lab_hours INTEGER
     */
    @Column(name = "lab_hours")
    private Integer labHours;

    /**
     * Semester number
     * Column: semester INTEGER
     */
    @Column(name = "semester")
    private Integer semester;

    // =====================================================
    // Classifiers (LEGACY FIELDS WITH _)
    // =====================================================

    /**
     * Course type code
     * Column: _course_type VARCHAR(32)
     * References: hemishe_h_course_type.code
     */
    @Column(name = "_course_type", length = 32)
    private String courseType;

    /**
     * Assessment type code
     * Column: _assessment_type VARCHAR(32)
     * References: hemishe_h_assessment_type.code
     * Examples: 'EXAM', 'CREDIT', 'DIFF_CREDIT'
     */
    @Column(name = "_assessment_type", length = 32)
    private String assessmentType;

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
     * Elective course flag
     * Column: is_elective BOOLEAN
     */
    @Column(name = "is_elective")
    private Boolean isElective;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if course is active
     *
     * @return true if active flag is true AND not deleted
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    /**
     * Check if course is elective
     *
     * @return true if isElective flag is true
     */
    public boolean isElective() {
        return Boolean.TRUE.equals(isElective);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", university='" + university + '\'' +
                ", creditCount=" + creditCount +
                ", active=" + active +
                '}';
    }
}
