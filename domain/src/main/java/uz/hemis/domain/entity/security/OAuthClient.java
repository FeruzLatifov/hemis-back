package uz.hemis.domain.entity.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import uz.hemis.common.auth.ClientType;
import uz.hemis.domain.entity.base.AuditableEntity;
import uz.hemis.domain.entity.university.University;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OAuth 2.0 B2B Machine Account (RFC 6749 §4.4 — client_credentials grant).
 *
 * <p><strong>Why separate from {@link User}:</strong></p>
 * <ul>
 *   <li>Machine lifecycle: 180-day secret rotation, IP whitelist, per-client rate limits</li>
 *   <li>No human policy: no MFA, no password expiry, no account lockout</li>
 *   <li>Polymorphic tenancy: UNIVERSITY_BACKEND → university_code, others → organization_id</li>
 * </ul>
 *
 * <p><strong>Used by:</strong></p>
 * <ul>
 *   <li>224 universitet backend (univer.php) — B2B sync</li>
 *   <li>MyGov SSO, OneID SSO — government integrations</li>
 *   <li>HEMIS internal services (analytics, notifications, …)</li>
 * </ul>
 *
 * <p><strong>Table:</strong> {@code oauth_client} (V003)</p>
 *
 * @see ClientType
 * @see OAuthClientRole
 * @since 2.1.0
 */
@Entity
@Table(name = "oauth_client")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class OAuthClient extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // OAuth 2.0 Standard Fields (RFC 6749)
    // =====================================================

    /**
     * OAuth client identifier (public).
     * Examples: {@code "univer_101"}, {@code "mygov_sync"}, {@code "oneid_callback"}.
     */
    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    /**
     * BCrypt hash of client secret. Compatible with {@code LegacyPasswordEncoder}.
     * Plain secret is returned only on creation/rotation — never stored.
     */
    @Column(name = "client_secret_hash", nullable = false, length = 255)
    private String clientSecretHash;

    /**
     * Human-readable display name — e.g. {@code "Toshkent TATU"}, {@code "MyGov Student Sync"}.
     */
    @Column(name = "client_name", nullable = false, length = 255)
    private String clientName;

    /** Discriminator: UNIVERSITY_BACKEND | EXTERNAL_SYSTEM | INTERNAL_SERVICE. */
    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 30)
    private ClientType clientType;

    // =====================================================
    // Polymorphic Tenancy (XOR — UNIVERSITY_BACKEND vs others)
    // =====================================================

    /**
     * University scope — only for {@code UNIVERSITY_BACKEND}.
     * FK → {@code hemishe_e_university(code)}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_code", referencedColumnName = "code")
    private University university;

    /**
     * Organization scope — for EXTERNAL_SYSTEM / INTERNAL_SERVICE clients.
     * FK → {@code organization(id)} (deferred in V010).
     *
     * <p>Raw UUID hozircha (Organization entity kelgusi phase'da qo'shiladi).</p>
     */
    @Column(name = "organization_id")
    private UUID organizationId;

    // =====================================================
    // Grant Configuration
    // =====================================================

    /**
     * Allowed OAuth grant types. Default: {@code ['client_credentials']}.
     * PostgreSQL {@code TEXT[]}. Validated via CHECK constraint.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "grant_types", nullable = false, columnDefinition = "text[]")
    private List<String> grantTypes = new ArrayList<>(List.of("client_credentials"));

    /**
     * OAuth 2.0 scopes (RFC 6749 §3.3) — per-client allowed permission codes.
     *
     * <p>Each element should match a {@code permission.code} value. Default
     * {@code 'rest-api'} is the legacy umbrella scope (means "all REST API access")
     * — preserves backward-compat with 224 OTM password flow.</p>
     *
     * <p>Narrow B2B clients (MyGov PINFL, OneID profile, …) override with
     * specific permission codes: {@code ['students.view', 'students.search']}.</p>
     *
     * <p>Effective authorities at token issuance =
     * {@code role permissions ∪ scopes}, narrowed by {@code scope} request param.</p>
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scopes", nullable = false, columnDefinition = "text[]")
    private List<String> scopes = new ArrayList<>(List.of("rest-api"));

    // =====================================================
    // Network-level Security (rules.md #5)
    // =====================================================

    /**
     * IP whitelist (CIDR blocks). {@code NULL} / empty = no restriction (DEV only).
     * Production: OTM office IPs, MyGov endpoint IPs, …
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_ip_cidr", columnDefinition = "text[]")
    private List<String> allowedIpCidr;

    /** mTLS required — future, high-trust clients (MyGov callback, E-Imzo). */
    @Column(name = "require_mtls", nullable = false)
    private Boolean requireMtls = Boolean.FALSE;

    // =====================================================
    // Rate Limiting
    // =====================================================

    /** Requests per minute. Default 60; large OTMs 300-1000. */
    @Column(name = "rate_limit_rpm", nullable = false)
    private Integer rateLimitRpm = 60;

    /** Token bucket burst size. */
    @Column(name = "rate_limit_burst", nullable = false)
    private Integer rateLimitBurst = 10;

    // =====================================================
    // Token Configuration
    // =====================================================

    /** Access token TTL — default 1 hour (shorter than human 12h). */
    @Column(name = "access_token_ttl_seconds", nullable = false)
    private Integer accessTokenTtlSeconds = 3600;

    /** Refresh token TTL — default 30 days. */
    @Column(name = "refresh_token_ttl_seconds")
    private Integer refreshTokenTtlSeconds = 2592000;

    // =====================================================
    // Lifecycle
    // =====================================================

    /** Active flag — can be disabled without deletion (suspension). */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    /** Expiration timestamp. {@code NULL} = never expires. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** Last successful token issuance timestamp. */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /** IP of last successful token request. */
    @Column(name = "last_used_ip", length = 45)
    private String lastUsedIp;

    // =====================================================
    // Secret Rotation Tracking
    // =====================================================

    /** Timestamp of last secret rotation. */
    @Column(name = "secret_rotated_at")
    private LocalDateTime secretRotatedAt;

    /** Secret version counter — incremented on every rotation. */
    @Column(name = "secret_version", nullable = false)
    private Integer secretVersion = 1;

    // =====================================================
    // Partner Contact
    // =====================================================

    /** Contact email — OTM IT team, MyGov team, … */
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    // =====================================================
    // Role Bindings (junction — OAuthClientRole)
    // =====================================================

    /**
     * Role bindings with audit (granted_by / granted_at).
     * Use {@link #bindRole(Role, String)} / {@link #unbindRole(Role)} instead of direct mutation.
     */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OAuthClientRole> roleBindings = new HashSet<>();

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if the client is currently usable for token issuance.
     *
     * @return {@code true} if active, not deleted, and not expired
     */
    public boolean isOperational() {
        if (!Boolean.TRUE.equals(isActive) || isDeleted()) {
            return false;
        }
        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Check whether this client supports a given OAuth grant type.
     *
     * @param grantType e.g. {@code "client_credentials"}, {@code "refresh_token"}
     * @return {@code true} if the grant type is configured
     */
    public boolean supportsGrant(String grantType) {
        return grantType != null && grantTypes != null && grantTypes.contains(grantType);
    }

    /**
     * Check whether this client has a given scope assigned.
     *
     * @param scope OAuth scope (= permission code)
     */
    public boolean hasScope(String scope) {
        return scope != null && scopes != null && scopes.contains(scope);
    }

    /**
     * @return flat set of {@link Role}s assigned to this client
     */
    public Set<Role> getRoles() {
        if (roleBindings == null || roleBindings.isEmpty()) {
            return new HashSet<>();
        }
        return roleBindings.stream()
                .map(OAuthClientRole::getRole)
                .collect(Collectors.toSet());
    }

    /**
     * Check whether this client has a given role by code.
     *
     * @param roleCode role code (e.g. {@code "OTM_API"})
     */
    public boolean hasRole(String roleCode) {
        if (roleCode == null || roleBindings == null) {
            return false;
        }
        return roleBindings.stream()
                .map(OAuthClientRole::getRole)
                .filter(java.util.Objects::nonNull)
                .anyMatch(r -> roleCode.equals(r.getCode()));
    }

    /**
     * Bind a role to this client. Creates or re-activates the junction row.
     *
     * @param role role to grant (must be non-null and persisted)
     * @param grantedBy subject that granted the role (username or system actor)
     */
    public void bindRole(Role role, String grantedBy) {
        if (role == null) {
            return;
        }
        boolean alreadyBound = roleBindings.stream()
                .anyMatch(b -> role.equals(b.getRole()));
        if (alreadyBound) {
            return;
        }
        // EmbeddedId stays empty — Hibernate's @MapsId on the junction's
        // @ManyToOne fields will populate {clientId, roleId} from {client.id, role.id}
        // on persist. Pre-setting here would race with this client's own @PrePersist
        // (id may be null when bindRole runs before save).
        OAuthClientRole binding = new OAuthClientRole();
        binding.setId(new OAuthClientRoleId());
        binding.setClient(this);
        binding.setRole(role);
        binding.setGrantedBy(grantedBy);
        binding.setGrantedAt(LocalDateTime.now());
        roleBindings.add(binding);
    }

    /**
     * Remove a role binding. Junction row is deleted via orphanRemoval.
     */
    public void unbindRole(Role role) {
        if (role == null || roleBindings == null) {
            return;
        }
        roleBindings.removeIf(b -> role.equals(b.getRole()));
    }

    /**
     * Mark this client as used (updates lastUsedAt / lastUsedIp).
     * Called from OAuthClientAuthenticationService on successful token issuance.
     */
    public void markUsed(String remoteIp) {
        this.lastUsedAt = LocalDateTime.now();
        this.lastUsedIp = remoteIp;
    }

    @Override
    public String toString() {
        return "OAuthClient{id=" + getId() + ", clientId='" + clientId + "', type=" + clientType + "}";
    }
}
