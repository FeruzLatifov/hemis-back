package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Hemis version classifier (platform versiyasi).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemis_version")
@Getter
@Setter
public class HemisVersion extends ReferenceEntity {
}
