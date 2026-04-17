package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Nationality classifier (millati).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "nationality")
@Getter
@Setter
public class Nationality extends ReferenceEntity {
}
