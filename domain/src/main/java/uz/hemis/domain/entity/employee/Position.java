package uz.hemis.domain.entity.employee;

import jakarta.persistence.*;
import lombok.*;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Position — lavozim klassifikatori
 *
 * <p>227 ta lavozim, position_types ga bog'liq (type_code FK).</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "position")
@Getter
@Setter
@NoArgsConstructor
public class Position extends ReferenceEntity {

    @Column(name = "type_code", nullable = false, length = 10)
    private String typeCode;
}
