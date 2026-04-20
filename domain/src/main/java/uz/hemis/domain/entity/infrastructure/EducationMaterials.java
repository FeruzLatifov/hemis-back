package uz.hemis.domain.entity.infrastructure;

import uz.hemis.domain.entity.base.BaseEntity;
import uz.hemis.domain.entity.university.University;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * EducationMaterials Entity (PHASE 5: Infrastructure)
 *
 * Represents education materials and textbooks data.
 * Table: hemishe_r_education_materials
 *
 * CRITICAL - Infrastructure Module:
 * - Part of PHASE 5 (Infrastructure metrics)
 * - Tracks subject count, textbooks count, and created materials grade
 * - Speciality-level tracking
 */
@Getter
@Setter
@Entity
@Table(name = "hemishe_r_education_materials")
@SQLRestriction("delete_ts IS NULL")
public class EducationMaterials extends BaseEntity {

    /**
     * University reference (FK to h_university)
     * Column: university_code VARCHAR
     */
    @Column(name = "university_code")
    private String university;

    /**
     * Education year reference (FK to h_education_year)
     * Column: education_year_code VARCHAR
     */
    @Column(name = "education_year_code")
    private String educationYear;

    /**
     * Speciality ID reference
     * Column: speciality_id VARCHAR
     */
    @Column(name = "speciality_id")
    private String specialityId;

    /**
     * Speciality code
     * Column: speciality_code VARCHAR
     */
    @Column(name = "speciality_code")
    private String specialityCode;

    /**
     * Speciality name (LOB for long text)
     * Column: speciality_name TEXT
     */
    @Column(name = "speciality_name", columnDefinition = "TEXT")
    private String specialityName;

    /**
     * Subject count
     * Column: subject_count INTEGER
     */
    @Column(name = "subject_count")
    private Integer subjectCount;

    /**
     * Textbooks count
     * Column: textbooks_count INTEGER
     */
    @Column(name = "textbooks_count")
    private Integer textbooksCount;

    /**
     * Created materials grade/score
     * Column: created_materials_grade INTEGER
     */
    @Column(name = "created_materials_grade")
    private Integer createdMaterialsGrade;
}
