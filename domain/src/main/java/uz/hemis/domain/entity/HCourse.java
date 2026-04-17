package uz.hemis.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

/**
 * HCourse classifier - hemishe_h_course (kurs: 1-kurs, 2-kurs, ...)
 * PK: code (String), not UUID
 *
 * Note: This is DIFFERENT from Course entity (hemishe_e_course) which is a subject/course entity
 */
@Entity
@Table(name = "course")
@Getter
@Setter
public class HCourse extends ReferenceEntity {
}
