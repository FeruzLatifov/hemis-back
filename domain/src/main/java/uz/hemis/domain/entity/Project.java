package uz.hemis.domain.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Project Entity
 *
 * Table: hemishe_e_project
 * Purpose: Loyihalar (Projects) management
 *
 * CRITICAL - Legacy Table Mapping:
 * - Table: hemishe_e_project (ministry.sql)
 * - All column names preserved from OLD-HEMIS
 * - Soft delete: @SQLRestriction("delete_ts IS NULL")
 * - Classifier columns (_university, _department, etc.) are VARCHAR, not UUID
 *
 * NO-RENAME • NO-DELETE • NO-BREAKING-CHANGES
 *
 * @since 2.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "hemishe_e_project")
@SQLRestriction("delete_ts IS NULL")
public class Project extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "name")
    private String name;

    @Column(name = "project_number")
    private String projectNumber;

    /**
     * University code (classifier reference)
     * Column: _university VARCHAR(255)
     */
    @Column(name = "_university", length = 255)
    private String university;

    /**
     * Department code (classifier reference)
     * Column: _department VARCHAR(255)
     */
    @Column(name = "_department", length = 255)
    private String department;

    /**
     * Project type code (classifier reference)
     * Column: _project_type VARCHAR(255)
     */
    @Column(name = "_project_type", length = 255)
    private String projectType;

    /**
     * Locality code (classifier reference)
     * Column: _locality VARCHAR(255)
     */
    @Column(name = "_locality", length = 255)
    private String locality;

    /**
     * Project currency code (classifier reference)
     * Column: _project_currency VARCHAR(255)
     */
    @Column(name = "_project_currency", length = 255)
    private String projectCurrency;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "position")
    private Integer position;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "_translations")
    private String translations;
}
