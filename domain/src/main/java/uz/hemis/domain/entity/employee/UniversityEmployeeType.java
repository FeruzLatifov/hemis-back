package uz.hemis.domain.entity.employee;

import uz.hemis.domain.entity.university.University;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * University employee type classifier (OTM xodim turlari).
 *
 * <p>Table: {@code university_employee_type} (V015).</p>
 * <p>Values: Boshqa | Administrativ-boshqaruv | Professor-o'qituvchi |
 * O'quv-yordamchi | Xizmat ko'rsatuvchi.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_employee_type")
@Getter
@Setter
public class UniversityEmployeeType extends ReferenceEntity {
}
