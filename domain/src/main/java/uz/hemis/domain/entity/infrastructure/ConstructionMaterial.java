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
 * @since 2.0.0
 */
@Entity
@Table(name = "construction_material")
@Getter
@Setter
public class ConstructionMaterial extends ReferenceEntity {
}
