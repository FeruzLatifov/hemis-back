package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Teacher Position Type Entity - Xodim lavozimlari klassifikatori
 *
 * <p>Table: hemishe_h_teacher_position_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Lavozimlar (227 ta):</p>
 * <ul>
 *   <li>11 - Stajer-o'qituvchi</li>
 *   <li>12 - O'qituvchi</li>
 *   <li>13 - Katta o'qituvchi</li>
 *   <li>14 - Dotsent</li>
 *   <li>15 - Professor</li>
 *   <li>... va boshqalar</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_h_teacher_position_type")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class TeacherPositionType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(name = "name_ru", length = 255)
    private String nameRu;

    @Column(name = "active")
    private Boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "create_ts")
    private LocalDateTime createTs;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    public boolean isDeleted() {
        return deleteTs != null;
    }
}
