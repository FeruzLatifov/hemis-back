package uz.hemis.domain.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
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
 * Base class for CUBA legacy classifier tables ({@code hemishe_h_*}).
 *
 * <p><strong>Pattern:</strong> CUBA {@code BaseCodeNameEntity} — naturalPK (code),
 * soft-delete (delete_ts), legacy audit (create_ts/update_ts).</p>
 *
 * <p><strong>Use cases:</strong> All 102 {@code hemishe_h_*} classifier tables
 * (gender, country, nationality, score_type, ...) — single source of truth for
 * classifier data (rules.md v2.0).</p>
 *
 * <p><strong>Columns (CUBA legacy naming):</strong></p>
 * <ul>
 *   <li>{@code code} VARCHAR(32) — PK</li>
 *   <li>{@code name, name_ru, name_en} — i18n labels</li>
 *   <li>{@code active} BOOLEAN — soft-disable flag</li>
 *   <li>{@code version} INTEGER — optimistic locking</li>
 *   <li>{@code create_ts, created_by} — creation audit</li>
 *   <li>{@code update_ts, updated_by} — update audit</li>
 *   <li>{@code delete_ts, deleted_by} — soft delete</li>
 * </ul>
 *
 * <p><strong>Soft delete:</strong> Use {@code @SQLRestriction("delete_ts IS NULL")}
 * on concrete entity class to filter deleted records automatically.</p>
 *
 * @since 2.1.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class LegacyClassifierEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "active")
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreatedDate
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createTs;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    /**
     * Soft delete check.
     *
     * @return true if this record is soft-deleted ({@code delete_ts IS NOT NULL})
     */
    public boolean isDeleted() {
        return deleteTs != null;
    }

    // ============================================================
    // API COMPATIBILITY LAYER — modern naming aliases
    // ============================================================
    // Legacy code expects ReferenceEntity-style getters (isActive, getCreatedAt, ...)
    // These bridge to CUBA field names without data duplication.

    /** Modern alias: {@code isActive()} → {@code active} (null-safe). */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    /** Modern alias: {@code getCreatedAt()} → {@code createTs}. */
    public LocalDateTime getCreatedAt() {
        return createTs;
    }

    /** Modern alias: {@code setCreatedAt(LocalDateTime)} → {@code setCreateTs(LocalDateTime)}. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createTs = createdAt;
    }

    /** Modern alias: {@code getUpdatedAt()} → {@code updateTs}. */
    public LocalDateTime getUpdatedAt() {
        return updateTs;
    }

    /** Modern alias: {@code setUpdatedAt(LocalDateTime)} → {@code setUpdateTs(LocalDateTime)}. */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updateTs = updatedAt;
    }

    /** Modern alias: {@code setActive(boolean)} → {@code setActive(Boolean)} (for primitive boolean callers). */
    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    /**
     * Equals based on {@code code} (natural PK).
     *
     * <p>Uses {@code instanceof} to work correctly with Hibernate proxies.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegacyClassifierEntity that)) return false;
        return code != null && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
