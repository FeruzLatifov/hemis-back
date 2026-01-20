package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Student Status Type Entity - Talaba holatlari klassifikatori
 *
 * <p>Table: hemishe_h_student_status_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Holatlar:</p>
 * <ul>
 *   <li>10 - Boshqa</li>
 *   <li>11 - O'qimoqda</li>
 *   <li>12 - Chetlashgan</li>
 *   <li>13 - Akademik ta'til</li>
 *   <li>14 - Bitirgan</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_h_student_status_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class StudentStatusType implements Serializable {

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
