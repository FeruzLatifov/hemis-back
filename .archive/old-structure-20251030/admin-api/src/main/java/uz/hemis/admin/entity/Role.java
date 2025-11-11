package uz.hemis.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Role entity (Security Group in CUBA)
 *
 * Maps to: sec_role table (CUBA legacy schema)
 * Purpose: User roles for permission management
 * Pattern: Users can have multiple roles, roles have multiple permissions
 */
@Entity
@Table(name = "sec_role", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Role name (unique identifier)
     * CUBA field: name
     * Examples: ADMIN, UNIVERSITY_ADMIN, DEAN, TEACHER, STUDENT
     */
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    /**
     * Human-readable role description
     * CUBA field: description
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Role type
     * CUBA field: type
     * Values: 0 = Standard, 1 = Super (read-only system role)
     */
    @Column(name = "type_")
    @Builder.Default
    private Integer type = 0;

    /**
     * Security scope
     * CUBA field: security_scope (for row-level security)
     * Values: null = no restrictions, UNIVERSITY = university-scoped
     */
    @Column(name = "security_scope", length = 255)
    private String securityScope;

    /**
     * Is default role for new users
     * CUBA field: is_default_role
     */
    @Column(name = "is_default_role")
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * CUBA audit fields
     */
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "update_ts")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "delete_ts")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    /**
     * Role-Permission mapping
     * Loaded separately when needed
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "sec_role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Users with this role
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    /**
     * Check if role is active (not deleted)
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Check if role is system role (read-only)
     */
    public boolean isSystemRole() {
        return Integer.valueOf(1).equals(type);
    }

    /**
     * Add permission to role
     */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    /**
     * Remove permission from role
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
