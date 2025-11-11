package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * IctEquipment Entity (PHASE 5: Infrastructure)
 *
 * Represents ICT equipment and projector inventory data.
 * Table: hemishe_r_ict_equipment
 *
 * CRITICAL - Infrastructure Module:
 * - Part of PHASE 5 (Infrastructure metrics)
 * - Tracks room count, projector counts (valid/invalid)
 * - Total count and grade calculations
 * - All FK are UUID (no ManyToOne)
 */
@Getter
@Setter
@Entity
@Table(name = "hemishe_r_ict_equipment")
@Where(clause = "delete_ts IS NULL")
public class IctEquipment extends BaseEntity {

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
     * Room count
     * Column: room_count INTEGER
     */
    @Column(name = "room_count")
    private Integer roomCount;

    /**
     * Valid projector count
     * Column: valid_projector_count INTEGER
     */
    @Column(name = "valid_projector_count")
    private Integer validProjectorCount;

    /**
     * Invalid projector count
     * Column: invalid_projector_count INTEGER
     */
    @Column(name = "invalid_projector_count")
    private Integer invalidProjectorCount;

    /**
     * Total count (aggregate)
     * Column: total_count INTEGER
     */
    @Column(name = "total_count")
    private Integer totalCount;

    /**
     * Total grade/score
     * Column: total_grade INTEGER
     */
    @Column(name = "total_grade")
    private Integer totalGrade;
}
