package uz.hemis.domain.entity.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * SOATO classifier (Davlat tasnifi — hududiy tartibi).
 *
 * <p>Hierarchical: parent_code NULL = region (4-digit), non-null = district (7-digit).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_soato")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class Soato extends LegacyClassifierEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_code", referencedColumnName = "code")
    private Soato parent;

    @Column(name = "parent_code", insertable = false, updatable = false, length = 20)
    private String parentCode;
}
