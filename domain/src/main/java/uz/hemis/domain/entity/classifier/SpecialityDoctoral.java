package uz.hemis.domain.entity.classifier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "hemishe_h_speciality_doctoral")
@SQLRestriction("delete_ts IS NULL")
public class SpecialityDoctoral extends BaseEntity {

    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name", length = 1024)
    private String name;

    // NOTE: hemishe_h_speciality_doctoral has only `name` (no name_en/name_ru,
    // unlike sibling classifiers). Mapping the missing columns made Hibernate
    // emit `sd1_0.name_en` → "column does not exist". Keep this entity to the
    // real table shape.
    @Column(name = "active")
    private Boolean active;
}
