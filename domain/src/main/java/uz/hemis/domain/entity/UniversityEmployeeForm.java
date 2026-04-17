package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Employment form classifier (xodim mehnat shakli).
 *
 * <p>Values: asosiy shtat / ichki o'rindoshlik / tashqi o'rindoshlik / soatbay</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "employment_form")
@Getter
@Setter
public class UniversityEmployeeForm extends ReferenceEntity {
}
