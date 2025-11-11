package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * Laboratories Entity (PHASE 5: Infrastructure)
 *
 * Represents laboratories and workshops inventory data.
 * Table: hemishe_r_laboratories
 *
 * CRITICAL - Infrastructure Module:
 * - Part of PHASE 5 (Infrastructure metrics)
 * - Tracks laboratories and workshops (valid/invalid counts)
 * - Speciality-level tracking with student counts
 * - Total aggregations and grade calculations
 * - All FK are UUID (no ManyToOne)
 */
@Getter
@Setter
@Entity
@Table(name = "hemishe_r_laboratories")
@Where(clause = "delete_ts IS NULL")
public class Laboratories extends BaseEntity {

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
     * Student count
     * Column: student_count INTEGER
     */
    @Column(name = "student_count")
    private Integer studentCount;

    /**
     * Valid laboratories count
     * Column: valid_laboratories_count INTEGER
     */
    @Column(name = "valid_laboratories_count")
    private Integer validLaboratoriesCount;

    /**
     * Valid workshops count
     * Column: valid_workshops_count INTEGER
     */
    @Column(name = "valid_workshops_count")
    private Integer validWorkshopsCount;

    /**
     * Invalid laboratories count
     * Column: invalid_laboratories_count INTEGER
     */
    @Column(name = "invalid_laboratories_count")
    private Integer invalidLaboratoriesCount;

    /**
     * Invalid workshops count
     * Column: invalid_workshops_count INTEGER
     */
    @Column(name = "invalid_workshops_count")
    private Integer invalidWorkshopsCount;

    /**
     * Total laboratories (aggregate)
     * Column: total_laboratories INTEGER
     */
    @Column(name = "total_laboratories")
    private Integer totalLaboratories;

    /**
     * Total workshops (aggregate)
     * Column: total_workshops INTEGER
     */
    @Column(name = "total_workshops")
    private Integer totalWorkshops;

    /**
     * Total grade/score
     * Column: total_grade INTEGER
     */
    @Column(name = "total_grade")
    private Integer totalGrade;
}
