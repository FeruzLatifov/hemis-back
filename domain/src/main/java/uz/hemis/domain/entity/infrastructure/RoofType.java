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
 * @since 2.0.0
 */
@Entity
@Table(name = "roof_type")
@Getter
@Setter
public class RoofType extends ReferenceEntity {
}
