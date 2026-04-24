package uz.hemis.domain.entity.finance;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

@Entity
@Table(name = "hemishe_h_contract_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class ContractType extends LegacyClassifierEntity {
}
