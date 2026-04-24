package uz.hemis.domain.entity.employee;

import uz.hemis.domain.entity.university.University;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * University employee type classifier (OTM xodim turlari).
 *
 * <p>Table: {@code university_employee_type} (V010).</p>
 * <p>Values: Boshqa | Administrativ-boshqaruv | Professor-o'qituvchi |
 * O'quv-yordamchi | Xizmat ko'rsatuvchi.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_university_employee_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityEmployeeType extends LegacyClassifierEntity {
}
