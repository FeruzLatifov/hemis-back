package uz.hemis.domain.entity.university;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * University parent-ministry classifier (OTM qaysi vazirlik tasarrufida).
 *
 * <p>Table: {@code university_belongs_to} (V010).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_university_belongs_to")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityBelongsTo extends LegacyClassifierEntity {
}
