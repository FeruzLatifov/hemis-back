package uz.hemis.domain.entity.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Bino kategoriyasi klassifikatori.
 * Excel template: docs/Бино ва иншоотлар жадвали.xlsx
 * 6 boshlang'ich turi: ACADEMIC, DORMITORY, ACTIVITY, SPORTS, UTILITY, RECREATION.
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "building_category")
@Getter
@Setter
public class BuildingCategory extends ReferenceEntity {
}
