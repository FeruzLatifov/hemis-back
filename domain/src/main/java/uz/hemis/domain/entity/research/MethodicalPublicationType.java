package uz.hemis.domain.entity.research;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Uslubiy nashr turlari klassifikatori
 * Jadval: hemishe_h_methodical_publication_type
 * Primary key: code (VARCHAR)
 */
@Getter
@Setter
@Entity
@Table(name = "hemishe_h_methodical_publication_type")
@SQLRestriction("delete_ts IS NULL")
public class MethodicalPublicationType extends LegacyClassifierEntity {
}
