package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * University Employee Status Type Entity - Xodim holatlari klassifikatori
 *
 * <p>Table: hemishe_h_university_employee_status_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Holatlar:</p>
 * <ul>
 *   <li>11 - Ishlamoqda</li>
 *   <li>12 - Ta'tilda</li>
 *   <li>13 - Xizmat safarida</li>
 *   <li>14 - Bo'shagan</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_h_university_employee_status_type")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityEmployeeStatusType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key - code (not UUID)
     * Values: 11, 12, 13, 14
     */
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

    /**
     * Check if entity is deleted (soft delete)
     */
    public boolean isDeleted() {
        return deleteTs != null;
    }
}
