package uz.hemis.domain.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Base for mutable entities that do NOT support soft delete.
 *
 * <p>Use when records are external snapshots (e.g. API sync) —
 * disappearing rows are kept as historical state, not marked deleted.</p>
 *
 * <p>Provides 5 audit columns: version, created_at/by, updated_at/by. No deleted_at.</p>
 *
 * <p><strong>Auditing:</strong> populated automatically via {@link AuditingEntityListener}
 * + {@code SecurityAuditorAware}. Requires {@code @EnableJpaAuditing}.</p>
 *
 * @since 2.0.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableEntityNoSoftDelete implements Serializable {

    @Id
    private UUID id;

    @Version
    private Integer version;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(updatable = false, length = 50)
    private String createdBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditableEntityNoSoftDelete that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
