package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Education type classifier (ta'lim turi — bakalavr, magistr, doktorantura).
 *
 * <p>Table: {@code education_type} (V009).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_education_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class EducationType extends LegacyClassifierEntity {
}
