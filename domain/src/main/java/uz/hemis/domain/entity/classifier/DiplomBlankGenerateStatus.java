package uz.hemis.domain.entity.classifier;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

@Entity
@Table(name = "diplom_blank_generate_status")
@Getter
@Setter
public class DiplomBlankGenerateStatus extends ReferenceEntity {
}
