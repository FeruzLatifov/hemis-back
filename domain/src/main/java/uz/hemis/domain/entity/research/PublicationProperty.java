package uz.hemis.domain.entity.research;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_publication_property")
@SQLRestriction("delete_ts IS NULL")
public class PublicationProperty extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "_university")
    private String university;

    @Column(name = "name", columnDefinition = "TEXT")
    private String name;

    @Column(name = "numbers")
    private String numbers;

    @Column(name = "authors", columnDefinition = "TEXT")
    private String authors;

    @Column(name = "author_counts")
    private Integer authorCounts;

    @Column(name = "parameter", columnDefinition = "TEXT")
    private String parameter;

    @Column(name = "property_date")
    private LocalDate propertyDate;

    @Column(name = "_patent_type")
    private String patentType;

    @Column(name = "_publication_database")
    private String publicationDatabase;

    @Column(name = "_locality")
    private String locality;

    @Column(name = "_country")
    private String country;

    @Column(name = "_employee")
    @ColumnTransformer(write = "CAST(? AS uuid)")
    private String employee;

    @Column(name = "filename", columnDefinition = "TEXT")
    private String filename;

    @Column(name = "position")
    private Integer position;

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
