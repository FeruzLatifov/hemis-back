package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

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
@Table(name = "hemishe_user")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class User extends BaseEntity {

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
     * Roles (comma-separated)
     * Column: roles VARCHAR(500)
     *
     * <p>Example: "ROLE_ADMIN,ROLE_UNIVERSITY_ADMIN"</p>
     * <p>Note: Spring Security requires ROLE_ prefix</p>
     *
     * <p><strong>Standard Roles:</strong></p>
     * <ul>
     *   <li>ROLE_ADMIN - Full system access</li>
     *   <li>ROLE_UNIVERSITY_ADMIN - University-specific admin</li>
     *   <li>ROLE_USER - Read-only access</li>
     * </ul>
     */
    @Column(name = "roles", length = 500)
    private String roles;

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
     * University code (nullable for system admins)
     * Column: _university VARCHAR(255)
     *
     * <p>Maps to UniversityUser.university in OLD-HEMIS</p>
     * <p>NULL for system administrators</p>
     * <p>Required for ROLE_UNIVERSITY_ADMIN users</p>
     *
     * <p><strong>Example:</strong></p>
     * <ul>
     *   <li>admin user: university = NULL (system-wide access)</li>
     *   <li>tatu_admin: university = "TATU" (only TATU data)</li>
     * </ul>
     */
    @Column(name = "_university", length = 255)
    private String university;

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
    // NOTE: NO DELETE METHODS
    // =====================================================
    // NDG (Non-Deletion Guarantee) - no physical DELETE
    // Soft delete handled at service layer by setting deleteTs
    // =====================================================
}
