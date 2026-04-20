package uz.hemis.domain.entity.research;

import uz.hemis.domain.entity.base.BaseEntity;
import uz.hemis.domain.entity.university.University;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;

/**
 * Ilmiy faoliyat entity.
 *
 * Database: hemishe_e_research_activity
 * Foreign keys:
 * - _university -> hemishe_e_university(code) [VARCHAR]
 * - _education_year -> hemishe_h_education_year(code) [VARCHAR]
 * - _scholar_database -> hemishe_h_scholar_database(code) [VARCHAR]
 */
@Getter
@Setter
@Entity
@Table(name = "hemishe_e_research_activity")
@SQLRestriction("delete_ts IS NULL")
public class ResearchActivity extends BaseEntity {

    /**
     * University code (foreign key to hemishe_e_university.code)
     * Database type: VARCHAR(255)
     */
    @Column(name = "_university", length = 255)
    private String university;

    /**
     * Education year code (foreign key to hemishe_h_education_year.code)
     * Database type: VARCHAR(32)
     */
    @Column(name = "_education_year", length = 32)
    private String educationYear;

    /**
     * Scholar database code (foreign key to hemishe_h_scholar_database.code)
     * Database type: VARCHAR(32)
     */
    @Column(name = "_scholar_database", length = 32)
    private String scholarDatabase;

    /**
     * Link to scholar database profile
     * Database type: TEXT
     */
    @Column(name = "link", columnDefinition = "TEXT")
    private String link;

    @Column(name = "h_index", length = 255)
    private String hIndex;

    @Column(name = "scientific_work_count", length = 255)
    private String scientificWorkCount;

    @Column(name = "reference_count", length = 255)
    private String referenceCount;
}
