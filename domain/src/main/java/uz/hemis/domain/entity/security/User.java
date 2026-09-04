package uz.hemis.domain.entity.security;

import uz.hemis.domain.entity.reference.Language;
import uz.hemis.domain.entity.university.University;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import uz.hemis.common.enums.UserType;
import uz.hemis.domain.entity.base.AuditableEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * User Entity - Authentication and Authorization
 *
 * <p><strong>CRITICAL - OAuth2 Compatibility:</strong></p>
 * <ul>
 *   <li>Table: users (NEW table for OAuth2)</li>
 *   <li>Purpose: Store university user credentials for API access</li>
 *   <li>Password: BCrypt hashed (never plain text)</li>
 *   <li>Roles: Comma-separated (ROLE_ADMIN, ROLE_OTM_API, ROLE_USER)</li>
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
 *   <li>@SQLRestriction("deleted_at IS NULL")</li>
 *   <li>Disabled users: enabled = false (NOT deleted)</li>
 *   <li>Deleted users: deleted_at != null</li>
 * </ul>
 *
 * @see AuditableEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class User extends AuditableEntity {

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
     * Roles (many-to-many relationship)
     * Join Table: user_role
     *
     * <p>Modern role management using proper entity relationships</p>
     * <p>Each user can have multiple roles (e.g., SUPER_ADMIN, ADMIN)</p>
     */
    @ManyToMany
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @BatchSize(size = 50)
    private Set<Role> roles = new HashSet<>();

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
     * User type (SYSTEM, UNIVERSITY, MINISTRY, ORGANIZATION)
     * Column: user_type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM'
     * CHECK constraint in DB: user_type IN ('SYSTEM', 'UNIVERSITY', 'MINISTRY', 'ORGANIZATION')
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 50)
    private UserType userType = UserType.SYSTEM;

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
     * First name (legacy old-hemis compatibility)
     * Column: first_name VARCHAR(255)
     */
    @Column(name = "first_name", length = 255)
    private String firstName;

    /**
     * Middle name (legacy old-hemis compatibility)
     * Column: middle_name VARCHAR(255)
     */
    @Column(name = "middle_name", length = 255)
    private String middleName;

    /**
     * Last name (legacy old-hemis compatibility)
     * Column: last_name VARCHAR(255)
     */
    @Column(name = "last_name", length = 255)
    private String lastName;

    /**
     * Position/title (legacy old-hemis compatibility)
     * Column: position VARCHAR(255)
     */
    @Column(name = "position", length = 255)
    private String position;

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

    /**
     * Time zone (legacy old-hemis compatibility)
     * Column: time_zone VARCHAR(50)
     */
    @Column(name = "time_zone", length = 50)
    private String timeZone;

    /**
     * Language (legacy old-hemis compatibility)
     * Column: language VARCHAR(20)
     * Example: "ru", "uz", "en"
     */
    @Column(name = "language", length = 20)
    private String language;

    /**
     * Locale (legacy old-hemis compatibility)
     * Column: locale VARCHAR(20)
     * Example: "uz", "ru", "en"
     */
    @Column(name = "locale", length = 20)
    private String locale;

    /**
     * University reference
     * Many-to-one relationship with University entity
     * Column: university_id VARCHAR(255) -> hemishe_e_university.code
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", referencedColumnName = "code")
    private University university;

    /**
     * Get university code (from university relation)
     * Replaces old entityCode field
     */
    public String getUniversityCode() {
        return university != null ? university.getCode() : null;
    }

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
     * <p>Lock account after 10 failed attempts</p>
     */
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    /**
     * Timestamp when the account was locked (auto-unlock after 15 minutes)
     * Column: locked_at TIMESTAMP
     *
     * <p>Set when account gets locked due to failed attempts</p>
     * <p>Used for auto-unlock: if lockedAt + 15 min &lt; now, auto-unlock</p>
     */
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    // =====================================================
    // Person Identity (PINFL anchor + optional employee bridge)
    // =====================================================

    /**
     * National ID — JSHSHIR/PINFL (14 digits). Person identity anchor.
     *
     * <p>Stored on {@code users} directly (not {@code employee}): ministry/org admins are
     * NOT OTM employees. <strong>PII</strong> — never log, never persist into audit JSONB
     * (recursive redaction handles nested cases). Uniqueness: partial-unique
     * {@code uq_users_pinfl}. {@code NULL} for machine-migrated/legacy rows until onboarding.</p>
     *
     * <p><strong>WRITE-ONCE.</strong> {@code updatable = false} is the enforcement, not a hint:
     * Hibernate leaves this column out of every UPDATE it generates, so no code path — a new field
     * on {@code UserUpdateRequest}, a stray setter, a mapper that copies whole entities — can change
     * a PINFL once the row exists. It is set on INSERT only ({@code UserAdminService.createUser}).
     *
     * <p>Why it must be immutable: the PINFL IS the person this login belongs to. Editing it does
     * not correct a typo, it silently re-points an account — with its roles, its audit history and
     * everything it has already signed off — at a different human being. A wrong PINFL is fixed by
     * deleting the account and creating the right one, which leaves a trail; an UPDATE leaves none.
     * The partial-unique {@code uq_users_pinfl} would not catch it either: moving a PINFL onto an
     * account that has none violates nothing.</p>
     */
    @Column(name = "pinfl", length = 14, updatable = false)
    private String pinfl;

    /**
     * Optional bridge → {@code employee(id)}. Set ONLY when this login is itself an OTM
     * employee (e.g. a rektor) — then join for OTM-HR data. PERMANENTLY optional: ministry
     * and organization admins are not employees; their identity is {@link #pinfl} here, so
     * PINFL never requires an employee join.
     */
    @Column(name = "employee_id")
    private UUID employeeId;

    /**
     * FK to {@code organization(id)} — ministry sub-org / external-body tenancy.
     *
     * <p>Raw {@code UUID} (not a lazy association) to keep reads self-contained; scope
     * resolution loads the org→OTM mapping explicitly in P3. {@code NULL} for
     * UNIVERSITY/MINISTRY/SYSTEM users.</p>
     */
    @Column(name = "organization_id")
    private UUID organizationId;

    // =====================================================
    // Person data (GUVD/api_mspd passport-data autofill) — "Shaxs" akkaunt turi.
    // Har bir tashqi-API maydoni o'z ustunida (V020). PII: passport ham read-gated.
    // =====================================================

    /** Tug'ilgan sana (GUVD: birth_date). Column: birth_date DATE */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** Tug'ilgan joy (GUVD: birth_place). Column: birth_place VARCHAR(255) */
    @Column(name = "birth_place", length = 255)
    private String birthPlace;

    /** Pasport seriya+raqam, masalan AA0000000 (GUVD: document). PII. Column: passport VARCHAR(16) */
    @Column(name = "passport", length = 16)
    private String passport;

    /** Pasport berilgan joy (GUVD: doc_give_place). Column: passport_give_place VARCHAR(255) */
    @Column(name = "passport_give_place", length = 255)
    private String passportGivePlace;

    /** Pasport berilgan sana (GUVD: issued_date). Column: passport_issued_date DATE */
    @Column(name = "passport_issued_date")
    private LocalDate passportIssuedDate;

    /** Pasport amal qilish muddati (GUVD: expiry_date). Column: passport_expiry_date DATE */
    @Column(name = "passport_expiry_date")
    private LocalDate passportExpiryDate;

    /** Jinsi (GUVD: sex). Column: gender VARCHAR(10) */
    @Column(name = "gender", length = 10)
    private String gender;

    /** Millati (GUVD: nationality). Column: nationality VARCHAR(64) */
    @Column(name = "nationality", length = 64)
    private String nationality;

    /** Ro'yxatdan o'tgan manzil (GUVD person-address). Column: address VARCHAR(512) */
    @Column(name = "address", length = 512)
    private String address;

    /** Shaxs fotosurati base64 (GUVD: photo). Og'ir; nullable. Column: photo TEXT */
    @Column(name = "photo", columnDefinition = "TEXT")
    private String photo;

    // =====================================================
    // Security Hardening (rules.md #5 — Security by default)
    // =====================================================

    /** Per-account requests-per-minute ceiling (enforced in RateLimitService). */
    @Column(name = "rate_limit_rpm")
    private Integer rateLimitRpm = 60;

    /** Timestamp of last password rotation. */
    @Column(name = "secret_rotated_at")
    private LocalDateTime secretRotatedAt;

    /** Policy expiry — after this, user must rotate secret on next login. */
    @Column(name = "secret_expires_at")
    private LocalDateTime secretExpiresAt;

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
     * Check if user has a specific role.
     * Accepts role code with or without {@code ROLE_} prefix (prefix is stripped).
     *
     * @param role role code (e.g. {@code "ADMIN"}, {@code "ROLE_ADMIN"}, {@code "SUPER_ADMIN"})
     * @return {@code true} if user has a matching role in {@link #roles}
     */
    public boolean hasRole(String role) {
        if (role == null || roles == null || roles.isEmpty()) {
            return false;
        }
        String target = role.startsWith("ROLE_") ? role.substring(5) : role;
        return roles.stream()
                .anyMatch(r -> target.equals(r.getCode()));
    }

    /** @return {@code true} if user has {@code SUPER_ADMIN} or {@code ADMIN} role */
    public boolean isSystemAdmin() {
        return hasRole("SUPER_ADMIN") || hasRole("ADMIN");
    }

    /** @return {@code true} if user has {@code UNIVERSITY_ADMIN} or {@code OTM_API} role */
    public boolean isUniversityAdmin() {
        return hasRole("UNIVERSITY_ADMIN") || hasRole("OTM_API");
    }

    // =====================================================
    // Role Management Methods (New Permission System)
    // =====================================================

    /**
     * Add role to user
     *
     * @param role Role to add
     */
    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
        role.getUsers().add(this);
    }

    /**
     * Remove role from user
     *
     * @param role Role to remove
     */
    public void removeRole(Role role) {
        if (roles != null) {
            roles.remove(role);
            role.getUsers().remove(this);
        }
    }

    /**
     * Check if user has specific role (by code)
     *
     * @param roleCode Role code (e.g., "SUPER_ADMIN", "ADMIN")
     * @return true if user has the role
     */
    public boolean hasRoleByCode(String roleCode) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> roleCode.equals(role.getCode()));
    }

    /**
     * Get all permissions from all roles
     *
     * @return Set of all permissions (merged from all roles)
     */
    public Set<Permission> getAllPermissions() {
        if (roles == null || roles.isEmpty()) {
            return new HashSet<>();
        }

        Set<Permission> allPermissions = new HashSet<>();
        for (Role role : roles) {
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
