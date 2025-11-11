package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Role Entity - User Role Management
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Define roles for users (e.g., MINISTRY_ADMIN, UNIVERSITY_ADMIN, VIEWER)</li>
 *   <li>Clean, modern permission system for hemis-back</li>
 *   <li>Independent from CUBA Platform's sec_role</li>
 * </ul>
 *
 * <p><strong>Table:</strong> hemishe_role</p>
 *
 * <p><strong>Default Roles:</strong></p>
 * <ul>
 *   <li>SUPER_ADMIN - Full system access (Ministry level)</li>
 *   <li>MINISTRY_ADMIN - Ministry-level administrator</li>
 *   <li>UNIVERSITY_ADMIN - University-level administrator</li>
 *   <li>VIEWER - Read-only access</li>
 *   <li>REPORT_VIEWER - Can view and generate reports</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * Primary Key (UUID)
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    // =====================================================
    // Role Identification
    // =====================================================

    /**
     * Role code (machine-readable)
     * <p>Examples: MINISTRY_ADMIN, UNIVERSITY_ADMIN, VIEWER</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    /**
     * Role name (human-readable)
     * <p>Examples: Ministry Administrator, University Administrator</p>
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Full description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Role type (for categorization)
     * <p>Values: SYSTEM, UNIVERSITY, CUSTOM</p>
     */
    @Column(name = "role_type", length = 50)
    private String roleType;

    /**
     * Active flag
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    // =====================================================
    // Audit Fields (CUBA Standard Pattern)
    // =====================================================

    /**
     * Creation timestamp
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Version (optimistic locking)
     */
    @Version
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    // =====================================================
    // Relationships
    // =====================================================

    /**
     * Permissions assigned to this role
     */
    @ManyToMany
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Users assigned to this role
     */
    @ManyToMany(mappedBy = "roleSet")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if role is active (not deleted, not disabled)
     *
     * @return true if role is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && deletedAt == null;
    }

    /**
     * Check if role is deleted (soft delete)
     *
     * @return true if deleted_at is not null
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if this is a system role (cannot be deleted)
     *
     * @return true if role_type is SYSTEM
     */
    public boolean isSystemRole() {
        return "SYSTEM".equals(roleType);
    }

    /**
     * Add permission to role
     *
     * @param permission Permission to add
     */
    public void addPermission(Permission permission) {
        permissions.add(permission);
        permission.getRoles().add(this);
    }

    /**
     * Remove permission from role
     *
     * @param permission Permission to remove
     */
    public void removePermission(Permission permission) {
        permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    /**
     * Add user to role
     *
     * @param user User to add
     */
    public void addUser(User user) {
        users.add(user);
        user.getRoles().add(this);
    }

    /**
     * Remove user from role
     *
     * @param user User to remove
     */
    public void removeUser(User user) {
        users.remove(user);
        user.getRoles().remove(this);
    }

    // =====================================================
    // PrePersist Hook
    // =====================================================

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (version == null) {
            version = 1;
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Equals & HashCode (based on ID)
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return id != null && id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
