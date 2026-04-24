package uz.hemis.domain.entity.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Employment form classifier (xodim mehnat shakli).
 *
 * <p>Table: {@code employment_form} (V009).</p>
 * <p>Values: Asosiy shtat | Ichki o'rindoshlik | Tashqi o'rindoshlik | Soatbay.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_university_employee_form")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class EmploymentForm extends LegacyClassifierEntity {
}
