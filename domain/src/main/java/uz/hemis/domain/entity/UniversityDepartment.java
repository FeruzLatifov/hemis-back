package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * University Department Entity - Bo'lim/Kafedra klassifikatori
 *
 * <p>Table: hemishe_e_university_department</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Ma'lumotlar:</p>
 * <ul>
 *   <li>305-106-12 - Energoaudit</li>
 *   <li>305-212 - Reja moliya bo'limi</li>
 *   <li>305-221 - Malaka oshirish va qayta tayyorlash markazi</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_university_department")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class UniversityDepartment implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Transient field to track if entity is new (for Spring Data JPA)
     * Assigned ID (String code) entities need this for proper persist vs merge behavior
     */
    @Transient
    private boolean isNew = true;

    @Id
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "name_uz")
    private String nameUz;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "university_code", length = 64)
    private String universityCode;

    @Column(name = "parent_code", length = 64)
    private String parentCode;

    @Column(name = "path")
    private String path;

    @Column(name = "_deparment_type", length = 32)
    private String departmentType;

    @Column(name = "status")
    private Boolean status;

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

    public String getName() {
        return nameUz != null ? nameUz : code;
    }

    public boolean isDeleted() {
        return deleteTs != null;
    }

    // =====================================================
    // Persistable<String> implementation
    // For entities with assigned (non-generated) ID
    // =====================================================

    @Override
    public String getId() {
        return code;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Mark entity as not new after loading from database or persisting
     * This is critical for Spring Data JPA to use persist() vs merge() correctly
     */
    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    /**
     * Set version to 1 for new entities before persist
     * Required because version column is NOT NULL
     */
    @PrePersist
    protected void onCreate() {
        if (version == null) {
            version = 1;
        }
        if (createTs == null) {
            createTs = LocalDateTime.now();
        }
    }

    /**
     * Update timestamp before update
     */
    @PreUpdate
    protected void onUpdate() {
        updateTs = LocalDateTime.now();
    }
}
