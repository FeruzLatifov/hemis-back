package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_publication_property")
@Where(clause = "delete_ts IS NULL")
public class PublicationProperty extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "_university")
    private UUID university;

    @Lob
    @Column(name = "name")
    private String name;

    @Column(name = "numbers")
    private String numbers;

    @Lob
    @Column(name = "authors")
    private String authors;

    @Column(name = "author_counts")
    private Integer authorCounts;

    @Lob
    @Column(name = "parameter")
    private String parameter;

    @Column(name = "property_date")
    private LocalDate propertyDate;

    @Column(name = "_patent_type")
    private UUID patentType;

    @Column(name = "_publication_database")
    private UUID publicationDatabase;

    @Column(name = "_locality")
    private UUID locality;

    @Column(name = "_country")
    private UUID country;

    @Column(name = "_employee")
    private UUID employee;

    @Lob
    @Column(name = "filename")
    private String filename;

    @Column(name = "position")
    private Integer position;

    @Column(name = "active")
    private Boolean active;

    @Lob
    @Column(name = "translations")
    private String translations;

    @Column(name = "is_checked")
    private Boolean isChecked;

    @Column(name = "is_checked_date")
    private LocalDateTime isCheckedDate;

    @Column(name = "_education_year")
    private UUID educationYear;
}
