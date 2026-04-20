package uz.hemis.domain.entity.university;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * University department type classifier (OTM bo'linma turlari).
 *
 * <p>Table: {@code university_department_type} (V015).</p>
 * <p>Values: Fakultet | Kafedra | Bo'lim | Markaz | va boshqalar.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_department_type")
@Getter
@Setter
public class UniversityDepartmentType extends ReferenceEntity {
}
