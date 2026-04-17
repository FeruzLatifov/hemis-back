package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * TransferType classifier - hemishe_h_transfer_type
 * PK: code (String), not UUID
 */
@Entity
@Table(name = "transfer_type")
@Getter
@Setter
public class TransferType extends ReferenceEntity {
}
