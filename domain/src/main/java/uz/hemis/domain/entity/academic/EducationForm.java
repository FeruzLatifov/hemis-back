package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Education form classifier (ta'lim shakli — kunduzgi, sirtqi, kechki).
 *
 * <p>Table: {@code education_form} (V009).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_education_form")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class EducationForm extends LegacyClassifierEntity {
}
