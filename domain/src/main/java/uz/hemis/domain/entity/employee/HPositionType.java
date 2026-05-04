package uz.hemis.domain.entity.employee;

import jakarta.persistence.*;
import lombok.*;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * HPositionType — lavozim turi klassifikatori
 *
 * <p>14 ta guruh: Rektorat, Akademik, Administrativ, Moliyaviy, va boshqalar.</p>
 *
 * <p>h_* prefiks: 224 OTM ekosistemi (hemis_NNN bazalari) konvensiyasi (ADR-0006).</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "h_position_type")
@Getter
@Setter
@NoArgsConstructor
public class HPositionType extends ReferenceEntity {
}
