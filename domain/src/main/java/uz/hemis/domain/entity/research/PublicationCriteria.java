package uz.hemis.domain.entity.research;

import uz.hemis.domain.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "hemishe_e_publication_criteria")
@SQLRestriction("delete_ts IS NULL")
public class PublicationCriteria extends BaseEntity {

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "_university")
    private String university;

    @Column(name = "_education_year")
    private String educationYear;

    @Column(name = "_publication_type_table")
    private String publicationTypeTable;

    @Column(name = "_publication_methodical_type")
    private String publicationMethodicalType;

    @Column(name = "_publication_scientific_type")
    private String publicationScientificType;

    @Column(name = "_publication_property_type")
    private String publicationPropertyType;

    @Column(name = "_in_publication_database")
    private Integer inPublicationDatabase;

    @Column(name = "mark_value")
    private Integer markValue;

    @Column(name = "position")
    private Integer position;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "_translations")
    private String translations;
}
