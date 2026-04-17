package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * University Employee Status Type Entity - Xodim holatlari klassifikatori
 *
 * <p>Table: hemishe_h_university_employee_status_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Holatlar:</p>
 * <ul>
 *   <li>11 - Ishlamoqda</li>
 *   <li>12 - Ta'tilda</li>
 *   <li>13 - Xizmat safarida</li>
 *   <li>14 - Bo'shagan</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "university_employee_status_type")
@Getter
@Setter
public class UniversityEmployeeStatusType extends ReferenceEntity {
}
