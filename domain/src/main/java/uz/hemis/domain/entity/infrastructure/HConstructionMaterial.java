package uz.hemis.domain.entity.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Qurilish materiali klassifikatori (g'isht, beton, panel, yog'och, ...).
 * Excel col 9: "Бинонинг асосий констукцияси материали"
 *
 * <p>h_* prefiks: 224 OTM ekosistemi (hemis_NNN bazalari) konvensiyasi (ADR-0006).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "h_construction_material")
@Getter
@Setter
public class HConstructionMaterial extends ReferenceEntity {
}
