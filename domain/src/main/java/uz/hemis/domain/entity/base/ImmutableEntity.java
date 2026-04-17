package uz.hemis.domain.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base for append-only/immutable records (lifecycle events, password history, ...).
 *
 * <p>Provides 2 audit columns: created_at, created_by.</p>
 * <p>No update, no delete — once written, never changed.</p>
 *
 * @since 2.0.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class ImmutableEntity implements Serializable {

    @Id
    private UUID id;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = false, length = 50)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
