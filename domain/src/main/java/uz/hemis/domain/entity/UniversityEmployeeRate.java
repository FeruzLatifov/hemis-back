package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * University Employee Rate Entity - Xodim mehnat stavkalari klassifikatori
 *
 * <p>Table: hemishe_h_university_employee_rate</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Stavkalar:</p>
 * <ul>
 *   <li>11 - 1,00 stavka</li>
 *   <li>12 - 0,75 stavka</li>
 *   <li>13 - 0,50 stavka</li>
 *   <li>14 - 0,25 stavka</li>
 *   <li>15 - 0,30 stavka</li>
 *   <li>16 - 0,20 stavka</li>
 *   <li>17 - 0,15 stavka</li>
 *   <li>18 - 0,10 stavka</li>
 *   <li>19 - 0,05 stavka</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_h_university_employee_rate")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityEmployeeRate implements Serializable {

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
