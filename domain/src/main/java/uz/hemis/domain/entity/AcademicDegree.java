package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * Academic degree classifier (ilmiy daraja).
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "academic_degree")
@Getter
@Setter
public class AcademicDegree extends ReferenceEntity {
}
