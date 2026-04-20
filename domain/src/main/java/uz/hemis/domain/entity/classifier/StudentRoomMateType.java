package uz.hemis.domain.entity.classifier;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.ReferenceEntity;

@Entity
@Table(name = "student_room_mate_type")
@Getter
@Setter
public class StudentRoomMateType extends ReferenceEntity {
}
