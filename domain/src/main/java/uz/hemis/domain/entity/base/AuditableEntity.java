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
 * Base for main business entities (employee, university_legal, organization, ...).
 *
 * <p>Provides 7 audit columns: version, created_at/by, updated_at/by, deleted_at/by.</p>
 * <p>Soft delete: {@code deleted_at IS NULL} = active.</p>
 *
 * <p><strong>Auditing:</strong> {@code createdAt/By} and {@code updatedAt/By} are populated
 * automatically by Spring Data JPA Auditing via {@link AuditingEntityListener} + {@code SecurityAuditorAware}.
 * Requires {@code @EnableJpaAuditing} on the application.</p>
 *
 * @since 2.0.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableEntity implements Serializable {

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

    private LocalDateTime deletedAt;

    @Column(length = 50)
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /** Soft delete this entity. {@code deletedBy} must be set by the service layer. */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /** Restore soft-deleted entity. */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditableEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
