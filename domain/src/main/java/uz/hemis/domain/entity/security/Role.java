package uz.hemis.domain.entity.security;

import uz.hemis.domain.entity.university.University;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.common.enums.RoleCode;
import uz.hemis.common.enums.RoleType;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Role Entity - User Role Management
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Define roles for users (e.g., MINISTRY_ADMIN, OTM_API, VIEWER)</li>
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
 *   <li>OTM_API - University-level administrator</li>
 *   <li>VIEWER - Read-only access</li>
 *   <li>REPORT_VIEWER - Can view and generate reports</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "role")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends AuditableEntity {

    // =====================================================
    // Role Identification
    // =====================================================

    /**
     * Role code (machine-readable)
     * <p>Examples: MINISTRY_ADMIN, OTM_API, VIEWER</p>
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
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 50)
    @Builder.Default
    private RoleType roleType = RoleType.CUSTOM;

    /**
     * Active flag
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    // =====================================================
    // Relationships
    // =====================================================

    /**
     * Permissions assigned to this role
     */
    @ManyToMany
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Users assigned to this role
     */
    @ManyToMany(mappedBy = "roles")
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
        return Boolean.TRUE.equals(active) && getDeletedAt() == null;
    }

    /**
     * Check if this is a system role (cannot be deleted)
     *
     * @return true if role_type is SYSTEM
     */
    public boolean isSystemRole() {
        return RoleType.SYSTEM.equals(roleType);
    }

    /**
     * Get RoleCode enum from code string
     *
     * @return RoleCode enum or null if not a standard role
     */
    public RoleCode getRoleCode() {
        return RoleCode.fromCode(this.code);
    }

    /**
     * Check if this role matches a standard RoleCode
     *
     * @param roleCode RoleCode to check
     * @return true if code matches
     */
    public boolean hasCode(RoleCode roleCode) {
        return roleCode != null && roleCode.getCode().equals(this.code);
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

    @Override
    public String toString() {
        return "Role{id=" + getId() + ", code='" + code + "', active=" + active + '}';
    }
}
