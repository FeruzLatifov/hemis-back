package uz.hemis.domain.entity.classifier;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.LegacyClassifierEntity;

@Entity
@Table(name = "hemishe_h_science_branch")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class ScienceBranch extends LegacyClassifierEntity {
}
