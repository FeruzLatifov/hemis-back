package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Student Status Type Entity - Talaba holatlari klassifikatori
 *
 * <p>Table: hemishe_h_student_status_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Holatlar:</p>
 * <ul>
 *   <li>10 - Boshqa</li>
 *   <li>11 - O'qimoqda</li>
 *   <li>12 - Chetlashgan</li>
 *   <li>13 - Akademik ta'til</li>
 *   <li>14 - Bitirgan</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_h_student_status_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class StudentStatusType extends LegacyClassifierEntity {
}
