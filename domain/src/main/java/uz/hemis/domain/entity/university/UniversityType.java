package uz.hemis.domain.entity.university;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * University type classifier (OTM turi).
 *
 * <p>Table: {@code university_type} (V015). Examples: davlat, nodavlat, xalqaro.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_type")
@Getter
@Setter
public class UniversityType extends ReferenceEntity {
}
