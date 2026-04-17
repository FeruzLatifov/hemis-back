package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * EducationType classifier - hemishe_h_education_type
 * PK: code (String), not UUID
 */
@Entity
@Table(name = "education_type")
@Getter
@Setter
public class EducationType extends ReferenceEntity {
}
