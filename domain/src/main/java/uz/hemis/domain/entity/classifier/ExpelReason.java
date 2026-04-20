package uz.hemis.domain.entity.classifier;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

@Entity
@Table(name = "expel")
@Getter
@Setter
public class ExpelReason extends ReferenceEntity {
}
