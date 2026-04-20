package uz.hemis.domain.entity.research;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_publication_methodical")
@SQLRestriction("delete_ts IS NULL")
public class PublicationMethodical extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "_university")
    private String university;

    @Column(name = "name", columnDefinition = "TEXT")
    private String name;

    @Column(name = "authors", columnDefinition = "TEXT")
    private String authors;

    @Column(name = "author_counts")
    private Integer authorCounts;

    @Column(name = "publisher", columnDefinition = "TEXT")
    private String publisher;

    @Column(name = "issue_year")
    private Integer issueYear;

    @Column(name = "source_name", columnDefinition = "TEXT")
    private String sourceName;

    @Column(name = "parameter", columnDefinition = "TEXT")
    private String parameter;

    @Column(name = "_methodical_publication_type")
    private String methodicalPublicationType;

    @Column(name = "_publication_database")
    private String publicationDatabase;

    @Column(name = "_employee")
    private UUID employee;

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
