package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * EducationYear classifier - hemishe_h_education_year
 * PK: code (String), not UUID
 */
@Entity
@Table(name = "education_year")
@Getter
@Setter
public class EducationYear extends ReferenceEntity {
}
