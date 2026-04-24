package uz.hemis.domain.entity.academic;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

@Entity
@Table(name = "hemishe_h_education_week_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class EducationWeekType extends LegacyClassifierEntity {
}
