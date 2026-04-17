package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Ownership classifier (mulkchilik shakli — davlat/nodavlat).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "ownership")
@Getter
@Setter
public class Ownership extends ReferenceEntity {
}
