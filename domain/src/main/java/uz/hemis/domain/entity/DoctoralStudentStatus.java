package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Doktorantura Talabasi Statusi Entity
 *
 * <p>Table: hemishe_h_doctoral_student_status</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Doktorantura talabasi statuslari:</p>
 * <ul>
 *   <li>Faol</li>
 *   <li>Tugatgan</li>
 *   <li>Chiqarilgan</li>
 *   <li>va boshqalar</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "doctoral_student_status")
@Getter
@Setter
public class DoctoralStudentStatus extends ReferenceEntity {
}
