package uz.hemis.domain.entity.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Hemis version classifier (platform versiyasi).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_hemis_version_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class HemisVersion extends LegacyClassifierEntity {
}
