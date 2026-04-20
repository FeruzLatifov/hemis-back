package uz.hemis.domain.entity.employee;

import uz.hemis.domain.entity.university.University;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * University employee status classifier (OTM xodim holatlari).
 *
 * <p>Table: {@code university_employee_status_type} (V015).</p>
 * <p>Values: Ishlamoqda | Ta'tilda | Xizmat safarida | Bo'shagan.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_employee_status_type")
@Getter
@Setter
public class UniversityEmployeeStatusType extends ReferenceEntity {
}
