package uz.hemis.domain.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base for many-to-many junction tables (user_role, role_permission, ...).
 *
 * <p>Provides 1 audit column: created_at.</p>
 * <p>No PK in base — subclass defines composite PK or surrogate.</p>
 *
 * @since 2.0.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class JunctionEntity implements Serializable {

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
