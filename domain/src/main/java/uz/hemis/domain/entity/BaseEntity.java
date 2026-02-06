package uz.hemis.domain.entity;

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
import java.util.UUID;

/**
 * Base Entity - CUBA Platform Audit Pattern
 *
 * <p>All entity tables in ministry.sql follow this pattern.</p>
 *
 * <p><strong>Audit Columns:</strong></p>
 * <ul>
 *   <li>id - UUID primary key</li>
 *   <li>version - Optimistic locking (JPA @Version)</li>
 *   <li>create_ts, created_by - Creation tracking</li>
 *   <li>update_ts, updated_by - Update tracking</li>
 *   <li>delete_ts, deleted_by - Soft delete pattern</li>
 * </ul>
 *
 * <p><strong>CRITICAL - Column Names:</strong></p>
 * <ul>
 *   <li>Column names match ministry.sql EXACTLY</li>
 *   <li>NO RENAME allowed - would break replication</li>
 *   <li>Lowercase with underscores (PostgreSQL unquoted identifiers)</li>
 * </ul>
 *
 * <p><strong>Soft Delete Pattern:</strong></p>
 * <ul>
 *   <li>Active records: delete_ts IS NULL</li>
 *   <li>Deleted records: delete_ts IS NOT NULL</li>
 *   <li>Use @SQLRestriction("delete_ts IS NULL") on entity classes</li>
 * </ul>
 *
 * @since 1.0.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key - UUID
     *
     * <p>CRITICAL: Column name is "id" (lowercase) in all tables.</p>
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Optimistic locking version
     *
     * <p>JPA @Version - automatically managed by Hibernate.</p>
     * <p>Prevents lost updates in concurrent transactions.</p>
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * Creation timestamp
     *
     * <p>Set automatically on INSERT via Spring Data JPA Auditing.</p>
     * <p>Column: create_ts TIMESTAMP(6) WITHOUT TIME ZONE</p>
     */
    @CreatedDate
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createTs;

    /**
     * Creator username
     *
     * <p>Set automatically on INSERT from security context via AuditorAware.</p>
     * <p>Column: created_by VARCHAR(50)</p>
     */
    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    /**
     * Last update timestamp
     *
     * <p>Set automatically on UPDATE via Spring Data JPA Auditing.</p>
     * <p>Column: update_ts TIMESTAMP(6) WITHOUT TIME ZONE</p>
     */
    @LastModifiedDate
    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    /**
     * Last updater username
     *
     * <p>Set automatically on UPDATE from security context via AuditorAware.</p>
     * <p>Column: updated_by VARCHAR(50)</p>
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * Soft delete timestamp
     *
     * <p>CRITICAL: Use soft delete, NOT physical DELETE.</p>
     * <ul>
     *   <li>NULL = active record</li>
     *   <li>NOT NULL = logically deleted</li>
     * </ul>
     * <p>Column: delete_ts TIMESTAMP(6) WITHOUT TIME ZONE</p>
     */
    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    /**
     * Soft delete performer username
     *
     * <p>Set when delete_ts is set.</p>
     * <p>Column: deleted_by VARCHAR(50)</p>
     */
    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    /**
     * Check if entity is deleted (soft delete)
     *
     * @return true if delete_ts is not null
     */
    public boolean isDeleted() {
        return deleteTs != null;
    }

    /**
     * JPA PrePersist callback
     *
     * <p>Set creation audit fields before INSERT.</p>
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (version == null) {
            version = 1;
        }
    }

    /**
     * Equals based on ID (UUID)
     *
     * <p>Two entities are equal if they have the same ID.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    /**
     * HashCode based on ID (UUID)
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
