package uz.hemis.domain.entity.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Employment form classifier (xodim mehnat shakli).
 *
 * <p>Table: {@code employment_form} (V014).</p>
 * <p>Values: Asosiy shtat | Ichki o'rindoshlik | Tashqi o'rindoshlik | Soatbay.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "employment_form")
@Getter
@Setter
public class EmploymentForm extends ReferenceEntity {
}
