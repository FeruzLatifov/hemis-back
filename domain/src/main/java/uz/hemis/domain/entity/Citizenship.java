package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

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
@Table(name = "citizenship")
@Getter
@Setter
public class Citizenship extends ReferenceEntity {
}
