package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * University Employee Type Entity - Xodim turlari klassifikatori
 *
 * <p>Table: hemishe_h_university_employee_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Turlar:</p>
 * <ul>
 *   <li>10 - Boshqa</li>
 *   <li>11 - Administrativ-boshqaruv xodim</li>
 *   <li>12 - Professor-o'qituvchi xodim</li>
 *   <li>13 - O'quv-yordamchi va texnik xodim</li>
 *   <li>14 - Xizmat ko'rsatuvchi xodim</li>
 * </ul>
 *
 * <p>OLD-HEMIS Compatible: soft-deleted yozuvlar ham qaytariladi</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_h_university_employee_type")
// @SQLRestriction removed for OLD-HEMIS compatibility - soft-deleted records should be returned
@Getter
@Setter
public class UniversityEmployeeType implements Serializable {

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
