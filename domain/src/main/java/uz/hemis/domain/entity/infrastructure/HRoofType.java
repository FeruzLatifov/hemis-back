package uz.hemis.domain.entity.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Tom qoplamasi turi (metall, cherepitsa, shifer, ruberoyd, ...).
 * Excel col 10: "Бино томининг ёпилиши тури"
 *
 * <p>h_* prefiks: 224 OTM ekosistemi (hemis_NNN bazalari) konvensiyasi (ADR-0006).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "h_roof_type")
@Getter
@Setter
public class HRoofType extends ReferenceEntity {
}
