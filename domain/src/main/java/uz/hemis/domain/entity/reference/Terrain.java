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
 * Terrain classifier (mahalla/hudud — SOATO bilan bog'liq).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_terrain")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class Terrain extends LegacyClassifierEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soato_code", referencedColumnName = "code")
    private Soato soato;

    @Column(name = "soato_code", insertable = false, updatable = false, length = 20)
    private String soatoCode;
}
