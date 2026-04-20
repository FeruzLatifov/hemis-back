package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Academic rank classifier (ilmiy unvon).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "academic_rank")
@Getter
@Setter
public class AcademicRank extends ReferenceEntity {
}
