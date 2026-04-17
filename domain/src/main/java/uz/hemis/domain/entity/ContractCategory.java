package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Contract category classifier (shartnoma kategoriyasi — to'lov turi).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "contract_category")
@Getter
@Setter
public class ContractCategory extends ReferenceEntity {
}
