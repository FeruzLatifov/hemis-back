package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Verification Type Entity - DTM verification turlari
 *
 * <p>Table: hemishe_h_verification_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_h_verification_type")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class VerificationType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key - code (not UUID)
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
}
