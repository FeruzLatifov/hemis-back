package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * Admission type classifier (qabul turi — grant, kontrakt, maqsadli, xorijiy).
 *
 * <p>Table: {@code admission_type} (V009).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "hemishe_h_admission_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class AdmissionType extends LegacyClassifierEntity {
}
