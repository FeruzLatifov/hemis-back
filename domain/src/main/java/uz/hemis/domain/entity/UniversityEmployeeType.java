package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * University Employee Type Entity - Xodim turlari klassifikatori
 *
 * <p>Table: hemishe_h_university_employee_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Turlar:</p>
 * <ul>
 *   <li>10 - Boshqa</li>
 *   <li>11 - Administrativ-boshqaruv xodim</li>
 *   <li>12 - Professor-o'qituvchi xodim</li>
 *   <li>13 - O'quv-yordamchi va texnik xodim</li>
 *   <li>14 - Xizmat ko'rsatuvchi xodim</li>
 * </ul>
 *
 * <p>OLD-HEMIS Compatible: soft-deleted yozuvlar ham qaytariladi</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "university_employee_type")
@Getter
@Setter
public class UniversityEmployeeType extends ReferenceEntity {
}
