package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * University parent-ministry classifier (OTM qaysi vazirlik tasarrufida).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_belongs_to")
@Getter
@Setter
public class UniversityBelongsTo extends ReferenceEntity {
}
