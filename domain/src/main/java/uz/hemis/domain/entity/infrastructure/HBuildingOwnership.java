package uz.hemis.domain.entity.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Bino egalik shakli klassifikatori.
 *
 * <p>3 shakl (V024 seed): {@code OWN} (o'z mulki), {@code OPERATIVE} (operativ boshqaruv —
 * davlat mulki, OTM ixtiyorида), {@code RENT} (ijara). {@code university_building.ownership_code}
 * FK target. Kadastr {@code subjects}'дан auto-derive mumkin (OTM INN egami).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "h_building_ownership")
@Getter
@Setter
public class HBuildingOwnership extends ReferenceEntity {
}
