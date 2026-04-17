package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Doktorantura Talabasi Turi Entity
 *
 * <p>Table: hemishe_h_doctoral_student_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Doktorantura talabasi turlari (21-ilova):</p>
 * <ul>
 *   <li>Asosiy doktorant</li>
 *   <li>Tayanch doktorant</li>
 *   <li>Mustaqil tadqiqotchi</li>
 *   <li>va boshqalar</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "doctoral_student_type")
@Getter
@Setter
public class DoctoralStudentType extends ReferenceEntity {
}
