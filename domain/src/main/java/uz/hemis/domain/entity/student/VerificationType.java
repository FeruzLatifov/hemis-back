package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Verification Type Entity - DTM verification turlari
 *
 * <p>Table: hemishe_h_verification_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_h_verification_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class VerificationType extends LegacyClassifierEntity {
}
