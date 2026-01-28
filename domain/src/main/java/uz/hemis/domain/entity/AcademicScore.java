package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

/**
 * AcademicScore Entity - Mapped to hemishe_r_academic_score table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_r_academic_score (Report table)</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @SQLRestriction("delete_ts IS NULL")</li>
 *   <li>Entity name (CUBA): hemishe_RAcademicScore</li>
 * </ul>
 *
 * <p>Represents academic score reports (o'zlashtirish ko'rsatkichlari).</p>
 * <p>R prefix = Report entity</p>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_r_academic_score")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AcademicScore extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // University Fields
    // =====================================================

    /**
     * University code
     * Column: university_code VARCHAR(255)
     */
    @Column(name = "university_code", length = 255)
    private String universityCode;

    /**
     * University name
     * Column: university_name TEXT
     */
    @Column(name = "university_name", columnDefinition = "TEXT")
    private String universityName;

    // =====================================================
    // Faculty Fields
    // =====================================================

    /**
     * Faculty code
     * Column: faculty_code VARCHAR(255)
     */
    @Column(name = "faculty_code", length = 255)
    private String facultyCode;

    /**
     * Faculty name
     * Column: faculty_name TEXT
     */
    @Column(name = "faculty_name", columnDefinition = "TEXT")
    private String facultyName;

    // =====================================================
    // Education Type Fields
    // =====================================================

    /**
     * Education type code
     * Column: education_type_code VARCHAR(255)
     */
    @Column(name = "education_type_code", length = 255)
    private String educationTypeCode;

    /**
     * Education type name
     * Column: education_type_name VARCHAR(255)
     */
    @Column(name = "education_type_name", length = 255)
    private String educationTypeName;

    // =====================================================
    // Education Year Fields
    // =====================================================

    /**
     * Education year code
     * Column: education_year_code VARCHAR(255)
     */
    @Column(name = "education_year_code", length = 255)
    private String educationYearCode;

    /**
     * Education year name
     * Column: education_year_name VARCHAR(255)
     */
    @Column(name = "education_year_name", length = 255)
    private String educationYearName;

    // =====================================================
    // Semester Fields
    // =====================================================

    /**
     * Semester type code
     * Column: semester_type_code VARCHAR(255)
     */
    @Column(name = "semester_type_code", length = 255)
    private String semesterTypeCode;

    /**
     * Semester type name
     * Column: semester_type_name VARCHAR(255)
     */
    @Column(name = "semester_type_name", length = 255)
    private String semesterTypeName;

    // =====================================================
    // Course Fields
    // =====================================================

    /**
     * Course code
     * Column: course_code VARCHAR(255)
     */
    @Column(name = "course_code", length = 255)
    private String courseCode;

    /**
     * Course name
     * Column: course_name VARCHAR(255)
     */
    @Column(name = "course_name", length = 255)
    private String courseName;

    // =====================================================
    // Score Fields
    // =====================================================

    /**
     * Table type - report category
     * Column: table_type VARCHAR(255)
     * Values: 'ўзлаштириш кўрсаткичлари', 'баҳолар кесимида талабаларининг ўзлаштириши', 'Академик қарздорлар'
     */
    @Column(name = "table_type", length = 255)
    private String tableType;

    /**
     * Score percent - o'zlashtirish %
     * Column: score_percent DOUBLE
     */
    @Column(name = "score_percent")
    private Double scorePercent;

    /**
     * Score type - baho turi
     * Column: score_type VARCHAR(255)
     * Values: 'qoniqarli', 'yaxshi', 'a\'lo'
     */
    @Column(name = "score_type", length = 255)
    private String scoreType;

    /**
     * Debitor count - qarzdorlar soni
     * Column: debitor_count DOUBLE
     */
    @Column(name = "debitor_count")
    private Double debitorCount;

    /**
     * Update date
     * Column: update_date DATE
     */
    @Column(name = "update_date")
    private LocalDate updateDate;

    @Override
    public String toString() {
        return "AcademicScore{" +
                "id=" + getId() +
                ", universityCode='" + universityCode + '\'' +
                ", facultyCode='" + facultyCode + '\'' +
                ", educationYearCode='" + educationYearCode + '\'' +
                ", scorePercent=" + scorePercent +
                ", scoreType='" + scoreType + '\'' +
                '}';
    }
}
