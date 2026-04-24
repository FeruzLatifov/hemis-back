package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Doctoral student type classifier (doktorantura talabasi turi — 21-ilova).
 *
 * <p>Table: {@code doctoral_student_type} (V010).</p>
 * <p>Values: Asosiy doktorant | Tayanch doktorant | Mustaqil tadqiqotchi | …</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_doctoral_student_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class DoctoralStudentType extends LegacyClassifierEntity {
}
