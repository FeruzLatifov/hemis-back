package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Doctoral student status classifier (doktorantura talabasi statusi).
 *
 * <p>Table: {@code doctoral_student_status} (V010).</p>
 * <p>Values: Faol | Tugatgan | Chiqarilgan | …</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_doctoral_student_status")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class DoctoralStudentStatus extends LegacyClassifierEntity {
}
