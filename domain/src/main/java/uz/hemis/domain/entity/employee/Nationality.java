package uz.hemis.domain.entity.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Nationality classifier (millati).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_nationality")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class Nationality extends LegacyClassifierEntity {
}
