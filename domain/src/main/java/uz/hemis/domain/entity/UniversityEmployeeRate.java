package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Employee rate classifier (xodim ish stavkasi).
 *
 * <p>Values: 1.00 / 0.75 / 0.50 / 0.25 / 0.30 / 0.20 / 0.15 / 0.10 / 0.05 stavka</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "employee_rate")
@Getter
@Setter
public class UniversityEmployeeRate extends ReferenceEntity {
}
