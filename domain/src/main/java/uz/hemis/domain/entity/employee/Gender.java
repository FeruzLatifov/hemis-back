package uz.hemis.domain.entity.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Gender classifier (jinsi).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "gender")
@Getter
@Setter
public class Gender extends ReferenceEntity {
}
