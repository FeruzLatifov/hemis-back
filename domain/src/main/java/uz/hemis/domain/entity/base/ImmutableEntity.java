package uz.hemis.domain.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Base for append-only/immutable records (lifecycle events, password history, ...).
 *
 * <p>Provides 2 audit columns: created_at, created_by.</p>
 * <p>No update, no delete — once written, never changed.</p>
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
public abstract class ImmutableEntity implements Serializable {

    @Id
    private UUID id;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(updatable = false, length = 50)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
