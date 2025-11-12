package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Authentication and Authorization
 *
 * <p><strong>CRITICAL - OAuth2 Compatibility:</strong></p>
 * <ul>
 *   <li>Table: hemishe_user (NEW table for OAuth2)</li>
 *   <li>Purpose: Store university user credentials for API access</li>
 *   <li>Password: BCrypt hashed (never plain text)</li>
 *   <li>Roles: Comma-separated (ROLE_ADMIN, ROLE_UNIVERSITY_ADMIN, ROLE_USER)</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Maps to CUBA's User and UniversityUser</li>
 *   <li>Username matches CUBA login</li>
 *   <li>University code preserved from UniversityUser</li>
 * </ul>
 *
 * <p><strong>Soft Delete Pattern:</strong></p>
 * <ul>
 *   <li>@Where(clause = "delete_ts IS NULL")</li>
 *   <li>Disabled users: enabled = false (NOT deleted)</li>
 *   <li>Deleted users: delete_ts != null</li>
 * </ul>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
public class User extends ModernBaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Authentication Fields
    // =====================================================

    /**
     * Username (login)
     * Column: username VARCHAR(255) NOT NULL UNIQUE
     *
     * <p>Used for OAuth2 password grant login</p>
     * <p>Example: "admin", "tatu_admin", "nuuz_user"</p>
     */
    @Column(name = "username", nullable = false, unique = true, length = 255)
    private String username;

    /**
     * Password (BCrypt hashed)
     * Column: password VARCHAR(255) NOT NULL
     *
     * <p><strong>CRITICAL:</strong> NEVER store plain text passwords!</p>
     * <p>Always use BCryptPasswordEncoder to hash</p>
     * <p>Example hash: "$2a$10$abcdef..." (60 characters)</p>
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    // =====================================================
    // Authorization Fields
    // =====================================================

    /**
     * Roles (comma-separated) - DEPRECATED
     *
     * <p><strong>DEPRECATED:</strong> Use roleSet instead for proper many-to-many relationship</p>
     * <p>Kept for backward compatibility with existing code that references this field</p>
     * <p>This field is @Transient - not persisted to database</p>
     * <p>Use getRoles() which returns Set<Role> from roleSet</p>
     */
    @Transient
    private String roles;

    /**
     * Roles (many-to-many relationship)
     * Join Table: hemishe_user_role
     *
     * <p>Modern role management using proper entity relationships</p>
     * <p>Each user can have multiple roles (e.g., SUPER_ADMIN, MINISTRY_ADMIN)</p>
     */
    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roleSet = new HashSet<>();

    /**
     * Enabled flag
     * Column: enabled BOOLEAN DEFAULT TRUE
     *
     * <p>Controls if user can login</p>
     * <p>Disabled users cannot get tokens (even with correct password)</p>
     * <p>Use this instead of soft delete for temporary account suspension</p>
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    // =====================================================
    // University Reference
    // =====================================================

    /**
     * Entity code (university/organization/ministry code, nullable for system admins)
     * Column: entity_code VARCHAR(255)
     *
     * <p>Maps to UniversityUser.university in OLD-HEMIS</p>
     * <p>NULL for system administrators</p>
     * <p>Required for non-SYSTEM user types</p>
     *
     * <p><strong>Example:</strong></p>
     * <ul>
     *   <li>admin user: entityCode = NULL (system-wide access)</li>
     *   <li>tatu_admin: entityCode = "TATU" (only TATU data)</li>
     * </ul>
     */
    @Column(name = "entity_code", length = 255)
    private String entityCode;

    // =====================================================
    // Personal Information (Optional)
    // =====================================================

    /**
     * Full name
     * Column: full_name VARCHAR(255)
     */
    @Column(name = "full_name", length = 255)
    private String fullName;

    /**
     * Email
     * Column: email VARCHAR(255)
     */
    @Column(name = "email", length = 255)
    private String email;

    /**
     * Phone number
     * Column: phone VARCHAR(50)
     */
    @Column(name = "phone", length = 50)
    private String phone;

    // =====================================================
    // Account Lockout (Security)
    // =====================================================

    /**
     * Account non-locked flag
     * Column: account_non_locked BOOLEAN DEFAULT TRUE
     *
     * <p>Set to false after N failed login attempts</p>
     * <p>Prevents brute force attacks</p>
     */
    @Column(name = "account_non_locked")
    private Boolean accountNonLocked = true;

    /**
     * Failed login attempts
     * Column: failed_attempts INTEGER DEFAULT 0
     *
     * <p>Reset to 0 on successful login</p>
     * <p>Lock account after 5 failed attempts</p>
     */
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if user account is active
     *
     * @return true if enabled AND not deleted AND not locked
     */
    public boolean isAccountActive() {
        return Boolean.TRUE.equals(enabled)
                && !isDeleted()
                && Boolean.TRUE.equals(accountNonLocked);
    }

    /**
     * Check if user has a specific role
     *
     * @param role role name (with or without ROLE_ prefix)
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }

        String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return this.roles.contains(roleToCheck);
    }

    /**
     * Check if user is system admin
     *
     * @return true if has ROLE_ADMIN
     */
    public boolean isSystemAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * Check if user is university admin
     *
     * @return true if has ROLE_UNIVERSITY_ADMIN
     */
    public boolean isUniversityAdmin() {
        return hasRole("ROLE_UNIVERSITY_ADMIN");
    }

    // =====================================================
    // Role Management Methods (New Permission System)
    // =====================================================

    /**
     * Get all roles assigned to this user
     *
     * @return Set of roles
     */
    public Set<Role> getRoles() {
        if (roleSet == null) {
            roleSet = new HashSet<>();
        }
        return roleSet;
    }

    /**
     * Add role to user
     *
     * @param role Role to add
     */
    public void addRole(Role role) {
        if (roleSet == null) {
            roleSet = new HashSet<>();
        }
        roleSet.add(role);
        role.getUsers().add(this);
    }

    /**
     * Remove role from user
     *
     * @param role Role to remove
     */
    public void removeRole(Role role) {
        if (roleSet != null) {
            roleSet.remove(role);
            role.getUsers().remove(this);
        }
    }

    /**
     * Check if user has specific role (by code)
     *
     * @param roleCode Role code (e.g., "SUPER_ADMIN", "MINISTRY_ADMIN")
     * @return true if user has the role
     */
    public boolean hasRoleByCode(String roleCode) {
        if (roleSet == null || roleSet.isEmpty()) {
            return false;
        }
        return roleSet.stream()
                .anyMatch(role -> roleCode.equals(role.getCode()));
    }

    /**
     * Get all permissions from all roles
     *
     * @return Set of all permissions (merged from all roles)
     */
    public Set<Permission> getAllPermissions() {
        if (roleSet == null || roleSet.isEmpty()) {
            return new HashSet<>();
        }

        Set<Permission> allPermissions = new HashSet<>();
        for (Role role : roleSet) {
            if (role.isActive() && role.getPermissions() != null) {
                allPermissions.addAll(role.getPermissions());
            }
        }
        return allPermissions;
    }

    /**
     * Check if user has specific permission
     *
     * @param permissionCode Permission code (e.g., "students.view", "reports.create")
     * @return true if user has the permission through any role
     */
    public boolean hasPermission(String permissionCode) {
        return getAllPermissions().stream()
                .anyMatch(permission -> permissionCode.equals(permission.getCode()));
    }

    /**
     * Get all permission codes as string array
     *
     * @return Array of permission codes
     */
    public String[] getPermissionCodes() {
        return getAllPermissions().stream()
                .map(Permission::getCode)
                .sorted()
                .toArray(String[]::new);
    }

    /**
     * Check if user has super admin role
     *
     * @return true if has SUPER_ADMIN role
     */
    public boolean isSuperAdmin() {
        return hasRoleByCode("SUPER_ADMIN");
    }

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // NDG (Non-Deletion Guarantee) - no physical DELETE
    // Soft delete handled at service layer by setting deleteTs
    // =====================================================
}
