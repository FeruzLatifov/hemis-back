package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_publication_scientific")
@SQLRestriction("delete_ts IS NULL")
public class PublicationScientific extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "_university")
    private String university;

    @Column(name = "name", columnDefinition = "TEXT")
    private String name;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "authors", columnDefinition = "TEXT")
    private String authors;

    @Column(name = "author_counts")
    private Integer authorCounts;

    @Column(name = "source_name", columnDefinition = "TEXT")
    private String sourceName;

    @Column(name = "issue_year")
    private Integer issueYear;

    @Column(name = "parameter", columnDefinition = "TEXT")
    private String parameter;

    @Column(name = "doi")
    private String doi;

    @Column(name = "_scientific_publication_type")
    private String scientificPublicationType;

    @Column(name = "_publication_database")
    private String publicationDatabase;

    @Column(name = "_locality")
    private String locality;

    @Column(name = "_country")
    private String country;

    @Column(name = "_employee")
    @ColumnTransformer(write = "CAST(? AS uuid)")
    private String employee;

    @Column(name = "filename")
    private String filename;

    @Column(name = "position")
    private String position;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "_translations", columnDefinition = "TEXT")
    private String translations;

    @Column(name = "is_checked")
    private Boolean isChecked;

    @Column(name = "is_checked_date")
    private LocalDateTime isCheckedDate;

    @Column(name = "_education_year")
    private String educationYear;
}
