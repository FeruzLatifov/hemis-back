package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

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
 * - All FK are UUID (no ManyToOne)
 */
@Getter
@Setter
@Entity
@Table(name = "hemishe_r_education_materials")
@Where(clause = "delete_ts IS NULL")
public class EducationMaterials extends BaseEntity {

    /**
     * University reference (FK to h_university)
     * Column: university_code UUID
     */
    @Column(name = "university_code")
    private UUID university;

    /**
     * Education year reference (FK to h_education_year)
     * Column: education_year_code UUID
     */
    @Column(name = "education_year_code")
    private UUID educationYear;

    /**
     * Speciality ID reference
     * Column: speciality_id UUID
     */
    @Column(name = "speciality_id")
    private UUID specialityId;

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
    @Lob
    @Column(name = "speciality_name")
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
