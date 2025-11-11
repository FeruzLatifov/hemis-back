package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_publication_author_meta")
@Where(clause = "delete_ts IS NULL")
public class PublicationAuthorMeta extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "_employee")
    private UUID employee;

    @Column(name = "is_main_author")
    private Integer isMainAuthor;

    @Column(name = "publication_type_table")
    private String publicationTypeTable;

    @Column(name = "_publication_methodical")
    private UUID publicationMethodical;

    @Column(name = "_publication_scientific")
    private UUID publicationScientific;

    @Column(name = "_publication_property")
    private UUID publicationProperty;

    @Column(name = "is_checked_by_author")
    private Boolean isCheckedByAuthor;

    @Column(name = "position")
    private Integer position;

    @Column(name = "active")
    private Boolean active;

    @Lob
    @Column(name = "translations")
    private String translations;

    @Column(name = "_university")
    private UUID university;
}
