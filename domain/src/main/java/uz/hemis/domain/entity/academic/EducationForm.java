package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Education form classifier (ta'lim shakli — kunduzgi, sirtqi, kechki).
 *
 * <p>Table: {@code education_form} (V014).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "education_form")
@Getter
@Setter
public class EducationForm extends ReferenceEntity {
}
