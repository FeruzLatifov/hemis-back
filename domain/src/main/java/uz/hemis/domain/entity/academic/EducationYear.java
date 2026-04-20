package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Academic year classifier (o'quv yili — 2024-2025, 2025-2026, ...).
 *
 * <p>Table: {@code education_year} (V014).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "education_year")
@Getter
@Setter
public class EducationYear extends ReferenceEntity {
}
