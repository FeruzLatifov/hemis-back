package uz.hemis.domain.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base for mutable entities that do NOT support soft delete.
 *
 * <p>Use when records are external snapshots (e.g. API sync) —
 * disappearing rows are kept as historical state, not marked deleted.</p>
 *
 * <p>Provides 5 audit columns: version, created_at/by, updated_at/by. No deleted_at.</p>
 *
 * @since 2.0.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntityNoSoftDelete implements Serializable {

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

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (version == null) version = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
