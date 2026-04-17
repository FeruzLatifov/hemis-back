package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Nashr Etish Hududi Entity
 *
 * <p>Table: hemishe_h_publication_locality</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Ilmiy loyiha va ilmiy nashrlarning joylari (33-ilova):</p>
 * <ul>
 *   <li>Respublika</li>
 *   <li>MDH</li>
 *   <li>Xorijiy</li>
 *   <li>va boshqalar</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "publication_locality")
@Getter
@Setter
public class PublicationLocality extends ReferenceEntity {
}
