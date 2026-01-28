package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SecUser Entity - Authentication (CUBA Platform Compatible)
 *
 * <p><strong>CRITICAL - old-hemis Compatibility:</strong></p>
 * <ul>
 *   <li>Table: sec_user (CUBA Platform legacy table)</li>
 *   <li>Purpose: Shared authentication between old-hemis and hemis-back</li>
 *   <li>Password: BCrypt (already in use by old-hemis)</li>
 *   <li>Soft Delete: delete_ts pattern (CUBA standard)</li>
 * </ul>
 *
 * <p><strong>Design Philosophy:</strong></p>
 * <ul>
 *   <li>READ-ONLY for authentication (hemis-back does NOT modify sec_user)</li>
 *   <li>All writes handled by old-hemis (single source of truth)</li>
 *   <li>Maps ALL columns but only uses essential ones</li>
 *   <li>Parallel operation: old-hemis (session) + hemis-back (JWT)</li>
 * </ul>
 *
 * <p><strong>Columns Mapping:</strong></p>
 * <pre>
 * sec_user.login        → username (for Spring Security)
 * sec_user.password     → password (BCrypt hash)
 * sec_user.active       → enabled (account status)
 * sec_user._university  → universityCode (OTM code)
 * </pre>
 *
 * @since 1.0.0
 * @see <a href="https://doc.cuba-platform.com/manual-7.2/users.html">CUBA User Entity</a>
 */
@Entity
@Table(name = "sec_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecUser {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key (UUID)
    // =====================================================

    /**
     * Primary Key
     * Column: id UUID PRIMARY KEY
     *
     * <p>CUBA uses UUID for all entities</p>
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    // =====================================================
    // Audit Fields (CUBA Standard)
    // =====================================================

    /**
     * Creation timestamp
     * Column: create_ts TIMESTAMP
     */
    @Column(name = "create_ts")
    private LocalDateTime createTs;

    /**
     * Created by (username)
     * Column: created_by VARCHAR(50)
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * Version (optimistic locking)
     * Column: version INTEGER
     */
    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * Update timestamp
     * Column: update_ts TIMESTAMP
     */
    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    /**
     * Updated by (username)
     * Column: updated_by VARCHAR(50)
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * Soft delete timestamp
     * Column: delete_ts TIMESTAMP
     *
     * <p>NULL = active user</p>
     * <p>NOT NULL = deleted user</p>
     */
    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    /**
     * Deleted by (username)
     * Column: deleted_by VARCHAR(50)
     */
    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    // =====================================================
    // Authentication Fields (Essential)
    // =====================================================

    /**
     * Login (username)
     * Column: login VARCHAR(50) NOT NULL UNIQUE
     *
     * <p>Example: "admin", "otm313", "hemis_user"</p>
     * <p>Used by Spring Security UserDetailsService</p>
     */
    @Column(name = "login", nullable = false, length = 50)
    private String login;

    /**
     * Login (lowercase)
     * Column: login_lc VARCHAR(50) NOT NULL UNIQUE
     *
     * <p>CUBA uses this for case-insensitive login</p>
     * <p>Automatically maintained by CUBA</p>
     */
    @Column(name = "login_lc", nullable = false, length = 50)
    private String loginLc;

    /**
     * Password (BCrypt hash)
     * Column: password VARCHAR(255)
     *
     * <p>Example: "$2a$10$gxb7T6V.q58ln..."</p>
     * <p>NEVER store plain text!</p>
     */
    @Column(name = "password", length = 255)
    private String password;

    /**
     * Password encryption type
     * Column: password_encryption VARCHAR(50)
     *
     * <p>Value: "bcrypt" (confirmed in test_hemis database)</p>
     * <p>Used by old-hemis for algorithm selection</p>
     */
    @Column(name = "password_encryption", length = 50)
    private String passwordEncryption;

    // =====================================================
    // Personal Information
    // =====================================================

    /**
     * Full name (display name)
     * Column: name VARCHAR(255)
     */
    @Column(name = "name", length = 255)
    private String name;

    /**
     * First name
     * Column: first_name VARCHAR(255)
     */
    @Column(name = "first_name", length = 255)
    private String firstName;

    /**
     * Last name
     * Column: last_name VARCHAR(255)
     */
    @Column(name = "last_name", length = 255)
    private String lastName;

    /**
     * Middle name (patronymic)
     * Column: middle_name VARCHAR(255)
     */
    @Column(name = "middle_name", length = 255)
    private String middleName;

    /**
     * Position (job title)
     * Column: position_ VARCHAR(255)
     *
     * <p>Note: Underscore suffix to avoid SQL keyword</p>
     */
    @Column(name = "position_", length = 255)
    private String position;

    /**
     * Email address
     * Column: email VARCHAR(100)
     */
    @Column(name = "email", length = 100)
    private String email;

    // =====================================================
    // Localization Settings
    // =====================================================

    /**
     * Language code
     * Column: language_ VARCHAR(20)
     *
     * <p>Example: "uz", "ru", "en"</p>
     */
    @Column(name = "language_", length = 20)
    private String language;

    /**
     * Time zone
     * Column: time_zone VARCHAR(50)
     *
     * <p>Example: "Asia/Tashkent"</p>
     */
    @Column(name = "time_zone", length = 50)
    private String timeZone;

    /**
     * Time zone auto-detect
     * Column: time_zone_auto BOOLEAN
     */
    @Column(name = "time_zone_auto")
    private Boolean timeZoneAuto;

    // =====================================================
    // Account Status
    // =====================================================

    /**
     * Active flag
     * Column: active BOOLEAN
     *
     * <p>TRUE = user can login</p>
     * <p>FALSE = account disabled</p>
     * <p>Maps to Spring Security "enabled"</p>
     */
    @Column(name = "active")
    private Boolean active;

    /**
     * Group ID (for role assignment)
     * Column: group_id UUID
     *
     * <p>References: sec_group table</p>
     * <p>CUBA uses groups for permission management</p>
     */
    @Column(name = "group_id")
    private UUID groupId;

    /**
     * IP mask (IP-based access control)
     * Column: ip_mask VARCHAR(200)
     *
     * <p>Example: "192.168.1.*,10.0.0.*"</p>
     * <p>Used by old-hemis for IP whitelisting</p>
     */
    @Column(name = "ip_mask", length = 200)
    private String ipMask;

    /**
     * Change password at next logon
     * Column: change_password_at_logon BOOLEAN
     */
    @Column(name = "change_password_at_logon")
    private Boolean changePasswordAtLogon;

    /**
     * Group names (comma-separated)
     * Column: group_names VARCHAR(255)
     *
     * <p>Denormalized field for performance</p>
     */
    @Column(name = "group_names", length = 255)
    private String groupNames;

    // =====================================================
    // Multi-tenancy (CUBA Feature)
    // =====================================================

    /**
     * System tenant ID
     * Column: sys_tenant_id VARCHAR(255)
     *
     * <p>NULL for single-tenant (most cases)</p>
     */
    @Column(name = "sys_tenant_id", length = 255)
    private String sysTenantId;

    /**
     * Discriminator type (CUBA inheritance)
     * Column: dtype VARCHAR(100)
     *
     * <p>Possible values:</p>
     * <ul>
     *   <li>"sec$User" - Standard user</li>
     *   <li>Custom types for extended entities</li>
     * </ul>
     */
    @Column(name = "dtype", length = 100)
    private String dtype;

    // =====================================================
    // University Reference (Custom Field)
    // =====================================================

    /**
     * University code
     * Column: _university VARCHAR(255)
     *
     * <p>References: hemishe_e_university.code</p>
     * <p>NULL for system admins</p>
     * <p>Required for OTM-specific users</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>NULL - System administrator (all universities)</li>
     *   <li>"TATU" - Tashkent University of Information Technologies</li>
     *   <li>"NUUZ" - National University of Uzbekistan</li>
     * </ul>
     */
    @Column(name = "_university", length = 255)
    private String universityCode;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if user is active (not deleted, not disabled)
     *
     * @return true if user can authenticate
     */
    public boolean isAccountActive() {
        return Boolean.TRUE.equals(active)
                && deleteTs == null;
    }

    /**
     * Check if user is deleted (soft delete)
     *
     * @return true if delete_ts is not null
     */
    public boolean isDeleted() {
        return deleteTs != null;
    }

    /**
     * Check if user is system admin (no university restriction)
     *
     * @return true if universityCode is null
     */
    public boolean isSystemAdmin() {
        return universityCode == null;
    }

    /**
     * Get username for Spring Security
     *
     * @return login field (username)
     */
    public String getUsername() {
        return login;
    }

    /**
     * Check if account is enabled (Spring Security interface)
     *
     * @return active flag
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(active);
    }

    /**
     * Check if account is not locked
     *
     * @return true (CUBA doesn't have account locking)
     */
    public boolean isAccountNonLocked() {
        return true; // CUBA doesn't use account locking by default
    }

    /**
     * Check if account is not expired
     *
     * @return true (CUBA doesn't have account expiration)
     */
    public boolean isAccountNonExpired() {
        return true; // CUBA doesn't use account expiration by default
    }

    /**
     * Check if credentials are not expired
     *
     * @return inverse of changePasswordAtLogon
     */
    public boolean isCredentialsNonExpired() {
        return !Boolean.TRUE.equals(changePasswordAtLogon);
    }

    // =====================================================
    // NOTE: READ-ONLY ENTITY
    // =====================================================
    // hemis-back should NOT modify sec_user table
    // All user management done by old-hemis (single source of truth)
    // This entity is ONLY for authentication (UserDetailsService)
    // =====================================================
}
