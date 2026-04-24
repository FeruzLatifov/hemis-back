package uz.hemis.domain.entity.university;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * University type classifier (OTM turi).
 *
 * <p>Table: {@code university_type} (V010). Examples: davlat, nodavlat, xalqaro.</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_university_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityType extends LegacyClassifierEntity {
}
