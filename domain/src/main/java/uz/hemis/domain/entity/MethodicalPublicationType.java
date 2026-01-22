package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Uslubiy nashr turlari klassifikatori
 * Jadval: hemishe_h_methodical_publication_type
 * Primary key: code (VARCHAR)
 */
@Getter
@Setter
@Entity
@Table(name = "hemishe_h_methodical_publication_type")
@SQLRestriction("delete_ts IS NULL")
public class MethodicalPublicationType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "active")
    private Boolean active;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createTs;

    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        if (version == null) {
            version = 1;
        }
        createTs = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTs = LocalDateTime.now();
    }
}
