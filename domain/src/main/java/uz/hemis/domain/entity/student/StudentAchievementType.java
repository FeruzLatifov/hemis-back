package uz.hemis.domain.entity.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

@Entity
@Table(name = "hemishe_h_student_achievement_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class StudentAchievementType extends LegacyClassifierEntity {

    private String parentCode;
}
