package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Admission type classifier (qabul turi — grant, kontrakt, maqsadli, xorijiy).
 *
 * <p>Table: {@code admission_type} (V014).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "admission_type")
@Getter
@Setter
public class AdmissionType extends ReferenceEntity {
}
