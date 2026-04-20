package uz.hemis.domain.entity.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Terrain classifier (mahalla/hudud — SOATO bilan bog'liq).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "terrain")
@Getter
@Setter
public class Terrain extends ReferenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soato_code", referencedColumnName = "code")
    private Soato soato;

    @Column(name = "soato_code", insertable = false, updatable = false, length = 20)
    private String soatoCode;
}
