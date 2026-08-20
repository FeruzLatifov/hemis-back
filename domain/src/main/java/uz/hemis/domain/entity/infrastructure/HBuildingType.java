package uz.hemis.domain.entity.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Bino turi klassifikatori (vazirlik-markaziy, 224 OTM'ga tarqatiladi).
 *
 * <p>35 tur, kod 11-45 (V023 seed). Univer'ning lokal vaqtinchalik {@code h_building_type}
 * (1-35) o'rniga markaziy manba. {@code university_building.building_type_code} FK target.</p>
 *
 * <p>Bino KATEGORIYASI ({@link HBuildingCategory}, 6 kod) — bu bilan aralashtirmaslik:
 * kategoriya = ixtiyoriy coarse rollup, tur = asosiy klassifikator.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "h_building_type")
@Getter
@Setter
public class HBuildingType extends ReferenceEntity {
}
