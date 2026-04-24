package uz.hemis.domain.entity.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Citizenship classifier (fuqarolik holati).
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>11 — O'zbekiston Respublikasi fuqarosi</li>
 *   <li>12 — Xorijiy davlat fuqarosi</li>
 *   <li>13 — Fuqaroligi yo'q shaxslar</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_citizenship")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class Citizenship extends LegacyClassifierEntity {
}
