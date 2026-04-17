package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * EducationForm classifier - hemishe_h_education_form
 * PK: code (String), not UUID
 */
@Entity
@Table(name = "education_form")
@Getter
@Setter
public class EducationForm extends ReferenceEntity {
}
