package uz.hemis.domain.entity.reference;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Ownership classifier (mulkchilik shakli — davlat/nodavlat).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_ownership")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class Ownership extends LegacyClassifierEntity {
}
