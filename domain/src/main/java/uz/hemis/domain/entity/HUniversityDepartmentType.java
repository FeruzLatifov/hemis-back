package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * OTM Bo'linma Turi Entity - University Department Type Classifier
 *
 * <p>Table: hemishe_h_university_department_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Bo'linma turlari:</p>
 * <ul>
 *   <li>Fakultet</li>
 *   <li>Kafedra</li>
 *   <li>Bo'lim</li>
 *   <li>Markaz</li>
 *   <li>va boshqalar</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "university_department_type")
@Getter
@Setter
public class HUniversityDepartmentType extends ReferenceEntity {
}
