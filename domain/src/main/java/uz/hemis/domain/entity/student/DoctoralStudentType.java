package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Doctoral student type classifier (doktorantura talabasi turi — 21-ilova).
 *
 * <p>Table: {@code doctoral_student_type} (V015).</p>
 * <p>Values: Asosiy doktorant | Tayanch doktorant | Mustaqil tadqiqotchi | …</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "doctoral_student_type")
@Getter
@Setter
public class DoctoralStudentType extends ReferenceEntity {
}
