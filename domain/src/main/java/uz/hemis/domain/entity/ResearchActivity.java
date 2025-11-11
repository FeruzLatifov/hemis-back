package uz.hemis.domain.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_research_activity")
@Where(clause = "delete_ts IS NULL")
public class ResearchActivity extends BaseEntity {

    @Column(name = "_university")
    private UUID university;

    @Column(name = "_education_year")
    private UUID educationYear;

    @Column(name = "_scholar_database")
    private UUID scholarDatabase;

    @Lob
    @Column(name = "link")
    private String link;

    @Column(name = "h_index")
    private String hIndex;

    @Column(name = "scientific_work_count")
    private String scientificWorkCount;

    @Column(name = "reference_count")
    private String referenceCount;
}
