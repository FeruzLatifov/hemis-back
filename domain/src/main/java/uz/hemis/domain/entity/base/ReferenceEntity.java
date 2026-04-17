package uz.hemis.domain.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base for classifier/reference tables (gender, soato, ownership, ...).
 *
 * <p>Provides 5 audit columns: version, created_at/by, updated_at/by.</p>
 * <p>No soft delete — classifiers use {@code isActive=false} instead.</p>
 * <p>PK = {@code code} (natural key, not UUID).</p>
 *
 * @since 2.0.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class ReferenceEntity implements Serializable {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    private String nameRu;

    private String nameEn;

    @Column(nullable = false)
    private boolean isActive = true;

    private Integer sortOrder;

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
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
