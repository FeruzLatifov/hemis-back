package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * PositionType — lavozim turi klassifikatori
 *
 * <p>14 ta guruh: Rektorat, Akademik, Administrativ, Moliyaviy, va boshqalar.</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "position_type")
@Getter
@Setter
@NoArgsConstructor
public class PositionType extends ReferenceEntity {
}
