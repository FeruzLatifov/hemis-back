package uz.hemis.domain.entity.university;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * University department type classifier (OTM bo'linma turlari).
 *
 * <p>Table: {@code university_department_type} (V010).</p>
 * <p>Values: Fakultet | Kafedra | Bo'lim | Markaz | va boshqalar.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_university_department_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityDepartmentType extends LegacyClassifierEntity {
}
