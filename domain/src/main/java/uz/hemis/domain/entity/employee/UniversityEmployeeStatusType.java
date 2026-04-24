package uz.hemis.domain.entity.employee;

import uz.hemis.domain.entity.university.University;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * University employee status classifier (OTM xodim holatlari).
 *
 * <p>Table: {@code university_employee_status_type} (V010).</p>
 * <p>Values: Ishlamoqda | Ta'tilda | Xizmat safarida | Bo'shagan.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_university_employee_status_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityEmployeeStatusType extends LegacyClassifierEntity {
}
