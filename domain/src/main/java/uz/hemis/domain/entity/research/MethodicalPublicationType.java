package uz.hemis.domain.entity.research;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Uslubiy nashr turlari klassifikatori
 * Jadval: hemishe_h_methodical_publication_type
 * Primary key: code (VARCHAR)
 */
@Getter
@Setter
@Entity
@Table(name = "methodical_publication_type")
public class MethodicalPublicationType extends ReferenceEntity {
}
