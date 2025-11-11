package uz.hemis.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Permission entity (Specific Permission in CUBA)
 *
 * Maps to: sec_permission table (CUBA legacy schema)
 * Purpose: Granular permissions for entity operations and screens
 * Format: entity:action or screen:view
 * Examples:
 *   - student:create, student:read, student:update, student:delete
 *   - grade:read, grade:update
 *   - dashboard:view, reports:view
 */
@Entity
@Table(name = "sec_permission", schema = "public",
    uniqueConstraints = @UniqueConstraint(columnNames = {"entity", "action"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Target entity/screen name
     * CUBA field: target
     * Examples: student, teacher, grade, enrollment, dashboard, reports
     */
    @Column(name = "entity", nullable = false, length = 100)
    private String entity;

    /**
     * Allowed action/operation
     * CUBA field: value
     * Values: create, read, update, delete, view, export, import
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Human-readable description
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Permission type
     * CUBA field: type
     * Values:
     *   10 = ENTITY_OP (entity operations)
     *   20 = ENTITY_ATTR (entity attributes)
     *   30 = SPECIFIC (specific permissions)
     *   40 = SCREEN (screen access)
     *   50 = UI (UI component)
     */
    @Column(name = "type_")
    @Builder.Default
    private Integer type = 10; // ENTITY_OP by default

    /**
     * Permission access value
     * CUBA field: access (0 = DENY, 1 = ALLOW, 2 = NOT_SET)
     * Values: 0 = deny, 1 = allow
     */
    @Column(name = "access")
    @Builder.Default
    private Integer access = 1; // ALLOW by default

    /**
     * CUBA audit fields
     */
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * Roles that have this permission
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Get full permission string (entity:action)
     */
    public String getFullPermission() {
        return entity + ":" + action;
    }

    /**
     * Check if permission allows access
     */
    public boolean isAllowed() {
        return Integer.valueOf(1).equals(access);
    }

    /**
     * Check if permission denies access
     */
    public boolean isDenied() {
        return Integer.valueOf(0).equals(access);
    }

    /**
     * Create from permission string
     * Format: "entity:action"
     */
    public static Permission fromString(String permissionStr) {
        String[] parts = permissionStr.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid permission format: " + permissionStr);
        }

        return Permission.builder()
            .entity(parts[0].trim())
            .action(parts[1].trim())
            .build();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission that)) return false;
        return entity.equals(that.entity) && action.equals(that.action);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(entity, action);
    }

    @Override
    public String toString() {
        return getFullPermission();
    }
}
