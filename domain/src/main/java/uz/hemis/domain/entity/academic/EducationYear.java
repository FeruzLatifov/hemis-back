package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Academic year classifier (o'quv yili — 2024-2025, 2025-2026, ...).
 *
 * <p>Table: {@code education_year} (V009).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_education_year")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class EducationYear extends LegacyClassifierEntity {
}
