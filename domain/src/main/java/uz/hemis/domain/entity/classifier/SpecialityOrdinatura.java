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
@Table(name = "hemishe_h_speciality_ordinatura")
@SQLRestriction("delete_ts IS NULL")
public class SpecialityOrdinatura extends BaseEntity {

    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name", length = 1024)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "active")
    private Boolean active;
}
