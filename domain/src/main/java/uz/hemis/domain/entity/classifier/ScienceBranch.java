package uz.hemis.domain.entity.classifier;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "hemishe_h_science_branch")
@SQLRestriction("delete_ts IS NULL")
public class ScienceBranch implements Serializable {

    @Id
    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "active")
    private Boolean active;

    @Version
    @Column(name = "version")
    private Integer version;
}
