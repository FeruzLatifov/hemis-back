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

/**
 * Base for classifier/reference tables (gender, soato, ownership, ...).
 *
 * <p>Provides 5 audit columns: version, created_at/by, updated_at/by.</p>
 * <p>No soft delete — classifiers use {@code isActive=false} instead.</p>
 * <p>PK = {@code code} (natural key, not UUID).</p>
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReferenceEntity that)) return false;
        return code != null && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
