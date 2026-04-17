package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * AdmissionType classifier - hemishe_h_admission_type
 * PK: code (String), not UUID
 */
@Entity
@Table(name = "admission_type")
@Getter
@Setter
public class AdmissionType extends ReferenceEntity {
}
