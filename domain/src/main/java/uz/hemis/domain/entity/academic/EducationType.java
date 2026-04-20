package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Education type classifier (ta'lim turi — bakalavr, magistr, doktorantura).
 *
 * <p>Table: {@code education_type} (V014).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "education_type")
@Getter
@Setter
public class EducationType extends ReferenceEntity {
}
