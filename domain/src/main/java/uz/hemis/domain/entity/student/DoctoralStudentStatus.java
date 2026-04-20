package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Doctoral student status classifier (doktorantura talabasi statusi).
 *
 * <p>Table: {@code doctoral_student_status} (V015).</p>
 * <p>Values: Faol | Tugatgan | Chiqarilgan | …</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "doctoral_student_status")
@Getter
@Setter
public class DoctoralStudentStatus extends ReferenceEntity {
}
