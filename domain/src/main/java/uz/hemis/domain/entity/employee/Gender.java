package uz.hemis.domain.entity.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Gender classifier (jinsi).
 *
 * <p>Mapped directly to legacy CUBA table {@code hemishe_h_gender} —
 * single source of truth per rules.md v2.0.</p>
 *
 * @since 2.1.0
 */
@Entity
@Table(name = "hemishe_h_gender")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class Gender extends LegacyClassifierEntity {
}
