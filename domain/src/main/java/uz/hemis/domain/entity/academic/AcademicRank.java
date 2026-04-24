package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Academic rank classifier (ilmiy unvon).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_academic_rank")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AcademicRank extends LegacyClassifierEntity {
}
