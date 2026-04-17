package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * University type classifier (OTM turi).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_type")
@Getter
@Setter
public class UniversityType extends ReferenceEntity {
}
