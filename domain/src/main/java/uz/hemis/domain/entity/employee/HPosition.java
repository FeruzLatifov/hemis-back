package uz.hemis.domain.entity.employee;

import jakarta.persistence.*;
import lombok.*;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * HPosition — lavozim klassifikatori
 *
 * <p>227 ta lavozim, h_position_type ga bog'liq (type_code FK).</p>
 *
 * <p>h_* prefiks: 224 OTM ekosistemi (hemis_NNN bazalari) konvensiyasi (ADR-0006).</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "h_position")
@Getter
@Setter
@NoArgsConstructor
public class HPosition extends ReferenceEntity {

    @Column(name = "type_code", nullable = false, length = 10)
    private String typeCode;
}
