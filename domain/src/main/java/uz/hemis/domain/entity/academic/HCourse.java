package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

/**
 * HCourse classifier - hemishe_h_course (kurs: 1-kurs, 2-kurs, ...)
 * PK: code (String), not UUID
 *
 * Note: This is DIFFERENT from Course entity (hemishe_e_course) which is a subject/course entity
 */
@Entity
@Table(name = "hemishe_h_course")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class HCourse extends LegacyClassifierEntity {
}
