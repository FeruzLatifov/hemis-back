package uz.hemis.domain.entity.finance;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Contract category classifier (shartnoma kategoriyasi — to'lov turi).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_university_contract_category")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class ContractCategory extends LegacyClassifierEntity {
}
