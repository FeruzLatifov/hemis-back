package uz.hemis.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity mapping to CUBA Platform sec$User table
 *
 * Maps to: sec_user table (CUBA legacy schema)
 * Purpose: Admin user accounts for HEMIS system
 * Security: Passwords stored as BCrypt hashes
 */
@Entity
@Table(name = "sec_user", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Unique login username
     * CUBA field: login
     */
    @Column(name = "login", unique = true, nullable = false, length = 50)
    private String username;

    /**
     * BCrypt password hash
     * CUBA field: password_hash
     * Format: $2a$10$... (BCrypt 60 chars)
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    /**
     * Full name of user
     * CUBA field: name
     */
    @Column(name = "name", length = 255)
    private String name;

    /**
     * Email address
     * CUBA field: email
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * Account active status
     * CUBA field: active
     * true = can login, false = disabled
     */
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    /**
     * Preferred locale
     * CUBA field: locale (stored in sec$UserSetting)
     * Values: uz, ru, en
     */
    @Column(name = "locale", length = 5)
    @Builder.Default
    private String locale = "uz";

    /**
     * Record creation timestamp
     * CUBA audit field: create_ts
     */
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record last update timestamp
     * CUBA audit field: update_ts
     */
    @Column(name = "update_ts")
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp
     * CUBA audit field: delete_ts
     * NULL = active, NOT NULL = soft deleted
     */
    @Column(name = "delete_ts")
    private LocalDateTime deletedAt;

    /**
     * User who created this record
     * CUBA audit field: created_by
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * User who last updated this record
     * CUBA audit field: updated_by
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * User who deleted this record
     * CUBA audit field: deleted_by
     */
    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    /**
     * University association
     * Loaded separately via UniversityUser mapping table
     */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UniversityUser universityUser;

    /**
     * User roles (security groups)
     * CUBA uses sec_group_hierarchy and sec_user_role
     * Many-to-Many relationship
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sec_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Get all permissions from all roles
     */
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>();
        for (Role role : roles) {
            allPermissions.addAll(role.getPermissions());
        }
        return allPermissions;
    }

    /**
     * Check if user has specific permission
     * Format: "entity:action" (e.g., "student:read")
     */
    public boolean hasPermission(String permissionStr) {
        String[] parts = permissionStr.split(":");
        if (parts.length != 2) return false;

        String entity = parts[0];
        String action = parts[1];

        return getAllPermissions().stream()
            .anyMatch(p -> p.getEntity().equals(entity) &&
                          p.getAction().equals(action) &&
                          p.isAllowed());
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
            .anyMatch(r -> r.getName().equals(roleName) && r.isActive());
    }

    /**
     * Get all permission strings
     */
    public Set<String> getPermissionStrings() {
        return getAllPermissions().stream()
            .filter(Permission::isAllowed)
            .map(Permission::getFullPermission)
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Check if user is not deleted
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && deletedAt == null;
    }

    /**
     * Check if user can login
     */
    public boolean canLogin() {
        return isActive();
    }

    /**
     * Soft delete the user
     */
    public void softDelete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.active = false;
    }

    /**
     * Restore soft-deleted user
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
        this.active = true;
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
