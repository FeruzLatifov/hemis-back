package uz.hemis.domain.entity.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Employee rate classifier (xodim ish stavkasi).
 *
 * <p>Table: {@code employee_rate} (V009).</p>
 * <p>Values: 1.00 | 0.75 | 0.50 | 0.25 | 0.30 | 0.20 | 0.15 | 0.10 | 0.05 stavka.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_university_employee_rate")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class EmployeeRate extends LegacyClassifierEntity {
}
