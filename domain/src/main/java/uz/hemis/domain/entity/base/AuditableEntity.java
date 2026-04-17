package uz.hemis.domain.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base for main business entities (employee, university_legal, organization, ...).
 *
 * <p>Provides 7 audit columns: version, created_at/by, updated_at/by, deleted_at/by.</p>
 * <p>Soft delete: {@code deleted_at IS NULL} = active.</p>
 *
 * @since 2.0.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity implements Serializable {

    @Id
    private UUID id;

    @Version
    private Integer version;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = false, length = 50)
    private String createdBy;

    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String updatedBy;

    private LocalDateTime deletedAt;

    @Column(length = 50)
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft delete this entity.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore soft-deleted entity.
     */
    public void restore() {
        this.deletedAt = null;
    }
}
