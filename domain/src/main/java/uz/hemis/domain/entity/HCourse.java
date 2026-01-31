package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * HCourse classifier - hemishe_h_course (kurs: 1-kurs, 2-kurs, ...)
 * PK: code (String), not UUID
 *
 * Note: This is DIFFERENT from Course entity (hemishe_e_course) which is a subject/course entity
 */
@Entity
@Table(name = "hemishe_h_course")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class HCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "name_ru", length = 255)
    private String nameRu;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "active")
    private Boolean active;

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
}
