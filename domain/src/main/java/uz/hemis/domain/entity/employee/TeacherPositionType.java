package uz.hemis.domain.entity.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Teacher Position Type Entity - Xodim lavozimlari klassifikatori
 *
 * <p>Table: hemishe_h_teacher_position_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Lavozimlar (227 ta):</p>
 * <ul>
 *   <li>11 - Stajer-o'qituvchi</li>
 *   <li>12 - O'qituvchi</li>
 *   <li>13 - Katta o'qituvchi</li>
 *   <li>14 - Dotsent</li>
 *   <li>15 - Professor</li>
 *   <li>... va boshqalar</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "teacher_position_type")
@Getter
@Setter
public class TeacherPositionType extends ReferenceEntity {
}
