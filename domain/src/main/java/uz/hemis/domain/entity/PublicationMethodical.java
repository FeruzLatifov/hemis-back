package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_publication_methodical")
@Where(clause = "delete_ts IS NULL")
public class PublicationMethodical extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "_university")
    private UUID university;

    @Lob
    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "authors")
    private String authors;

    @Column(name = "author_counts")
    private Integer authorCounts;

    @Lob
    @Column(name = "publisher")
    private String publisher;

    @Column(name = "issue_year")
    private Integer issueYear;

    @Lob
    @Column(name = "source_name")
    private String sourceName;

    @Lob
    @Column(name = "parameter")
    private String parameter;

    @Column(name = "_methodical_publication_type")
    private UUID methodicalPublicationType;

    @Column(name = "_publication_database")
    private UUID publicationDatabase;

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
