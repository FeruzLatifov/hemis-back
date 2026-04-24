package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * TransferType classifier - hemishe_h_transfer_type
 * PK: code (String), not UUID
 */
@Entity
@Table(name = "hemishe_h_transfer_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class TransferType extends LegacyClassifierEntity {
}
