package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modern Base Entity - Clean Spring Boot Style
 *
 * <p>Modern naming convention for new tables (users, roles, permissions)</p>
 *
 * <p><strong>Column Names:</strong></p>
 * <ul>
 *   <li>created_at (NOT create_ts)</li>
 *   <li>updated_at (NOT update_ts)</li>
 *   <li>deleted_at (NOT delete_ts)</li>
 * </ul>
 *
 * <p><strong>Soft Delete Pattern:</strong></p>
 * <ul>
 *   <li>Active records: deleted_at IS NULL</li>
 *   <li>Deleted records: deleted_at IS NOT NULL</li>
 *   <li>Use @Where(clause = "deleted_at IS NULL") on entity classes</li>
 * </ul>
 *
 * @since 2.0.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class ModernBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key - UUID
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Creation timestamp
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp
     * <p>NULL = active, NOT NULL = deleted</p>
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =====================================================
    // JPA Lifecycle Hooks
    // =====================================================

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if entity is deleted (soft delete)
     *
     * @return true if deleted_at is not null
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft delete this entity
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore soft-deleted entity
     */
    public void restore() {
        this.deletedAt = null;
    }
}
