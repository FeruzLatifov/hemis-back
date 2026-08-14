package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import uz.hemis.common.auth.SubjectType;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.domain.entity.security.OAuthClient;
import uz.hemis.domain.entity.security.Permission;
import uz.hemis.domain.entity.security.Role;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Token Service - JWT Token Generation (Stateless)
 *
 * <p><strong>CRITICAL - sec_user Compatibility:</strong></p>
 * <ul>
 *   <li>Generates JWT tokens (stateless - no Redis/database needed)</li>
 *   <li>Compatible with old-hemis user structure (sec_user)</li>
 *   <li>Token contains: username, roles, university, expiration</li>
 *   <li>Response format matches OAuth2 standard</li>
 * </ul>
 *
 * <p><strong>Token Claims:</strong></p>
 * <pre>
 * {
 *   "sub": "admin",                    // Subject (username)
 *   "iss": "hemis-backend",            // Issuer
 *   "iat": 1234567890,                 // Issued at
 *   "exp": 1234567890,                 // Expiration
 *   "roles": ["ROLE_ADMIN"],           // User roles
 *   "username": "admin"                // Username (redundant but useful)
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserPermissionCacheService permissionCacheService;
    private final uz.hemis.common.port.security.UserIdentificationPort userIdentificationPort;
    /**
     * Refresh token rotation — old jti blacklisted on consume (replay detection).
     * Optional (autowire by type, may be null in unit tests without Redis).
     */
    private final org.springframework.beans.factory.ObjectProvider<TokenBlacklistService> blacklistProvider;

    // Default 12 hours (43200 sec) — OWASP & security/CLAUDE.md (rule). Old-hemis 30-day default
    // edi, lekin leaked token'lar 30 kun valid bo'lar edi. application.yml ENV bilan override qiladi.
    @Value("${hemis.security.jwt.expiration:43200}")
    private long accessTokenValiditySeconds;

    @Value("${hemis.security.jwt.refresh-expiration:604800}")  // 7 days
    private long refreshTokenValiditySeconds;

    @Value("${hemis.security.jwt.issuer:hemis-backend}")
    private String issuer;

    /** Key ID for JWS header — supports key rotation (kid header per OAuth2/OIDC RFC 7515 §4.1.4). */
    @Value("${hemis.security.jwt.key-id:hemis-jwt-default}")
    private String keyId;

    /**
     * OTM (client_credentials) machine token TTL in seconds — a single ministry-wide policy.
     * Default 24h (env {@code OAUTH_CLIENT_TOKEN_EXPIRATION}). OTM backends are system-to-system
     * callers that cache and reuse the token for its whole lifetime, so a short TTL only forced
     * needless re-requests. Intentionally SEPARATE from the human/web JWT TTL
     * ({@code hemis.security.jwt.expiration}, 1h per ADR-0009): lengthening machine tokens must
     * never lengthen interactive user sessions.
     */
    @Value("${hemis.security.oauth.client-token-expiration:86400}")
    private int clientTokenExpirationSeconds;

    /**
     * Boot-time sanity guard for token TTLs. Catches the classic milliseconds-as-seconds
     * misconfiguration (e.g. {@code JWT_EXPIRATION=86400000} → ~1000-day access tokens).
     * <strong>Non-fatal by design</strong>: never blocks startup (so it can never break the 224-OTM
     * integration whose machine tokens are minted here) — but instead of merely logging, it now
     * <strong>clamps</strong> an out-of-range TTL to a sane ceiling, so a milliseconds-as-seconds
     * misconfig can no longer mint effectively-non-expiring tokens. The real fix is still to set the
     * value in SECONDS; the clamp is the fail-safe.
     */
    @jakarta.annotation.PostConstruct
    void validateTokenTtls() {
        final long maxSaneAccessSeconds = 86_400;      // 24h
        final long maxSaneRefreshSeconds = 2_592_000;  // 30d
        if (accessTokenValiditySeconds > maxSaneAccessSeconds) {
            log.error("JWT access-token TTL = {}s (~{} days) exceeds the sane max of {}s. "
                    + "Milliseconds-as-seconds misconfig (JWT_EXPIRATION) — set it in SECONDS (e.g. 3600). "
                    + "CLAMPING to {}s so tokens are not effectively non-expiring.",
                    accessTokenValiditySeconds, accessTokenValiditySeconds / 86_400, maxSaneAccessSeconds, maxSaneAccessSeconds);
            accessTokenValiditySeconds = maxSaneAccessSeconds; // fail-safe clamp — soft-fail (never blocks boot / the 224-OTM mint path)
        }
        if (refreshTokenValiditySeconds > maxSaneRefreshSeconds) {
            log.error("JWT refresh-token TTL = {}s (~{} days) exceeds the sane max of {}s. "
                    + "Milliseconds-as-seconds misconfig (JWT_REFRESH_EXPIRATION) — set it in SECONDS (e.g. 604800). "
                    + "CLAMPING to {}s.",
                    refreshTokenValiditySeconds, refreshTokenValiditySeconds / 86_400, maxSaneRefreshSeconds, maxSaneRefreshSeconds);
            refreshTokenValiditySeconds = maxSaneRefreshSeconds; // fail-safe clamp
        }
        // Lower-bound floor: a zero/negative TTL (e.g. JWT_EXPIRATION=0) would mint already-expired
        // tokens — clamp up so the guard is symmetric (still soft-fail, never blocks boot).
        if (accessTokenValiditySeconds < 60) {
            log.error("JWT access-token TTL = {}s is below the 60s floor (misconfig) — clamping to 60s.",
                    accessTokenValiditySeconds);
            accessTokenValiditySeconds = 60;
        }
        if (refreshTokenValiditySeconds < accessTokenValiditySeconds) {
            log.error("JWT refresh-token TTL = {}s below access TTL {}s (misconfig) — clamping to match access.",
                    refreshTokenValiditySeconds, accessTokenValiditySeconds);
            refreshTokenValiditySeconds = accessTokenValiditySeconds;
        }
    }

    /** Build JWS header with HS256 algorithm + kid (key rotation support).
     *  Defensive: keyId null/blank bo'lsa (test/profile yo'q), kid'siz quriladi. */
    private JwsHeader jwsHeader() {
        JwsHeader.Builder b = JwsHeader.with(MacAlgorithm.HS256);
        if (keyId != null && !keyId.isBlank()) {
            b = b.keyId(keyId);
        }
        return b.build();
    }

    /**
     * Generate OAuth2 JWT token
     *
     * <p><strong>Process:</strong></p>
     * <ol>
     *   <li>Extract username and roles from UserDetails</li>
     *   <li>Build JWT claims set</li>
     *   <li>Encode JWT using JwtEncoder (HMAC-SHA256)</li>
     *   <li>Return TokenResponse (OAuth2 format)</li>
     * </ol>
     *
     * <p><strong>Response Format (OAuth2):</strong></p>
     * <pre>
     * {
     *   "access_token": "eyJhbGciOiJIUzI1NiIs...",
     *   "token_type": "bearer",
     *   "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
     *   "expires_in": 43199,
     *   "scope": "rest-api"
     * }
     * </pre>
     *
     * @param userDetails authenticated user (from sec_user)
     * @return TokenResponse containing JWT access token and refresh token
     */
    public TokenResponse generateToken(UserDetails userDetails) {
        // ✅ HYBRID: Try users table first, fallback to sec_user
        var userInfo = userIdentificationPort.getUserInfoByUsername(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found in 'users' or 'sec_user' tables: {}", userDetails.getUsername());
                    return new IllegalArgumentException("User not found: " + userDetails.getUsername());
                });
        String userId = userInfo.id().toString();
        String fullName = userInfo.fullName();
        log.info("Generating JWT token for userId: {}", userId);

        // Current time
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenValiditySeconds);

        // ✅ Extract and cache permissions (not in JWT!)
        Set<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // ✅ Cache permissions in Redis by userId (UUID) - TTL: 1 hour
        permissionCacheService.cacheUserPermissions(java.util.UUID.fromString(userId), permissions);
        log.info("✅ Cached {} permissions for userId: {}", permissions.size(), userId);

        // ✅ MINIMAL JWT - No permissions, only essential claims
        // jti (RFC 7519 §4.1.7) — token blacklist key sifatida ishlatamiz (CookieJwtAuthenticationFilter check qiladi)
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())         // ✅ jti — unique token ID for blacklist
                .issuer(issuer)                          // Issuer: hemis-backend
                .issuedAt(now)                            // Issued at: now
                .expiresAt(expiry)                        // Expires at: now + 30 days
                .subject(userId)                          // ✅ Subject: userId (UUID string)
                .claim("username", userDetails.getUsername())  // Username claim (for display)
                // OWASP A09 — `full_name` claim REMOVED. Token leaks PII to localStorage,
                // browser history, proxy/Sentry logs (Bearer often logged). 1.15M users
                // continuous PII surface. Frontend `/api/v1/web/auth/me` endpoint orqali oladi.
                .claim("scope", "rest-api")              // Scope claim (OAuth2)
                .build();

        // Encode JWT with explicit headers (algorithm must match JWK)
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader(), claims)).getTokenValue();

        // ✅ Generate refresh token (old-hemis compatibility)
        String refreshToken = generateRefreshToken(userDetails);

        log.info("JWT token generated for userId: {} (expires in {} seconds)",
                userId, accessTokenValiditySeconds);

        // Build OAuth2 token response (OLD-HEMIS format WITH refresh_token)
        return TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("bearer")  // OLD-HEMIS uses lowercase "bearer"
                .refreshToken(refreshToken)  // ✅ old-hemis compatibility - WITH refresh_token
                .expiresIn((int) (accessTokenValiditySeconds - 2))  // OLD-HEMIS uses expiration - 2 seconds
                .scope("rest-api")
                .build();
    }

    /**
     * Generate refresh token
     *
     * @param userDetails user details
     * @return refresh token JWT
     */
    public String generateRefreshToken(UserDetails userDetails) {
        // ✅ HYBRID: Try users table first, fallback to sec_user
        String userId = getUserId(userDetails.getUsername());
        log.info("Generating refresh token for userId: {}", userId);

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenValiditySeconds);

        // ✅ MINIMAL JWT - No permissions, only essential claims + jti for blacklist support
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())         // ✅ jti — unique token ID for refresh rotation
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(userId)                          // ✅ Subject: userId (UUID string)
                .claim("username", userDetails.getUsername())  // Username (for display)
                .claim("type", "refresh")  // Mark as refresh token
                .build();

        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader(), claims)).getTokenValue();

        log.info("Refresh token generated for userId: {} (expires in {} days)",
                userId, refreshTokenValiditySeconds / 86400);

        return refreshToken;
    }

    /**
     * Refresh access token using refresh token
     *
     * <p><strong>IMPORTANT:</strong> This implementation generates tokens from JWT claims directly
     * without database lookup, making it stateless and avoiding circular dependencies.</p>
     *
     * @param refreshToken refresh token JWT
     * @return new TokenResponse with access and refresh tokens
     * @throws IllegalArgumentException if refresh token is invalid or expired
     */
    public TokenResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        try {
            // Decode and validate refresh token
            Jwt jwt = jwtDecoder.decode(refreshToken);

            // Check if it's a refresh token
            String tokenType = jwt.getClaimAsString("type");
            if (!"refresh".equals(tokenType)) {
                throw new IllegalArgumentException("Invalid token type: not a refresh token");
            }

            // OWASP A04 — Refresh token rotation + replay detection.
            // 1) Old jti must NOT be blacklisted (replay attempt).
            // 2) After successful refresh: blacklist old jti for remaining lifetime
            //    (so the same refresh token cannot be used twice).
            String oldJti = jwt.getId();
            TokenBlacklistService blacklistService = blacklistProvider.getIfAvailable();
            if (oldJti != null && blacklistService != null && blacklistService.isBlacklisted(oldJti)) {
                // Replay attempt — token already consumed. Suspicious.
                log.warn("SECURITY: refresh token replay detected — jti={} already blacklisted", oldJti);
                throw new IllegalArgumentException("Refresh token already consumed");
            }

            // Extract userId from JWT 'sub' claim
            String userIdString = jwt.getSubject();

            if (userIdString == null || userIdString.isEmpty()) {
                throw new IllegalArgumentException("UserId not found in refresh token");
            }

            java.util.UUID userId = java.util.UUID.fromString(userIdString);
            String username = jwt.getClaimAsString("username");  // Optional (for logging)
            // OWASP A09 — fullName JWT'dan olib tashlandi (PII leak fix). Frontend /me orqali oladi.

            log.debug("Refreshing token for userId: {} (username: {})", userId, username);

            // Generate new tokens directly from claims (stateless approach)
            Instant now = Instant.now();
            Instant accessExpiry = now.plusSeconds(accessTokenValiditySeconds);
            Instant refreshExpiry = now.plusSeconds(refreshTokenValiditySeconds);

            // ✅ MINIMAL JWT - No permissions, only essential claims + jti for blacklist
            JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                    .id(UUID.randomUUID().toString())     // ✅ jti — unique token ID
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(accessExpiry)
                    .subject(userId.toString())  // ✅ userId (UUID)
                    .claim("username", username)  // Username (for display)
                    // OWASP A09 — full_name removed (frontend /me endpoint orqali oladi).
                    .claim("scope", "rest-api")
                    .build();

            String newAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader(), accessClaims)).getTokenValue();

            // ✅ MINIMAL JWT - No permissions, only essential claims + jti for refresh rotation
            JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                    .id(UUID.randomUUID().toString())     // ✅ jti — unique token ID for rotation
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(refreshExpiry)
                    .subject(userId.toString())  // ✅ userId (UUID)
                    .claim("username", username)  // Username (for display)
                    .claim("type", "refresh")
                    .build();

            String newRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader(), refreshClaims)).getTokenValue();

            // Rotation — blacklist consumed refresh jti for remaining lifetime.
            if (oldJti != null && blacklistService != null && jwt.getExpiresAt() != null) {
                blacklistService.addToBlacklist(oldJti, jwt.getExpiresAt());
                log.debug("Refresh rotation — old jti {} blacklisted until {}",
                        oldJti, jwt.getExpiresAt());
            }

            // Build response (OLD-HEMIS format)
            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("bearer")  // OLD-HEMIS uses lowercase "bearer"
                    .expiresIn((int) (accessTokenValiditySeconds - 2))  // OLD-HEMIS uses expiration - 2 seconds
                    .scope("rest-api")
                    .build();

            log.info("Access token refreshed successfully for userId: {}", userId);

            return tokenResponse;

        } catch (JwtException e) {
            log.warn("Invalid or expired refresh token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired refresh token", e);
        }
    }

    /**
     * Get userId by username - Uses UserIdentificationPort (Clean Architecture)
     *
     * <p><strong>Hybrid Lookup (via Port):</strong></p>
     * <ol>
     *   <li>First try users table (new structure)</li>
     *   <li>If not found, fallback to sec_user table (old-hemis)</li>
     * </ol>
     *
     * @param username the username to lookup
     * @return userId as String (UUID)
     * @throws IllegalArgumentException if user not found in either table
     */
    private String getUserId(String username) {
        return userIdentificationPort.getUserIdByUsername(username)
                .map(uuid -> {
                    log.debug("User found: {} -> {}", username, uuid);
                    return uuid.toString();
                })
                .orElseThrow(() -> {
                    log.error("User not found in 'users' or 'sec_user' tables: {}", username);
                    return new IllegalArgumentException("User not found: " + username);
                });
    }

    // =====================================================
    // MACHINE TOKENS (Phase 2.6 — client_credentials grant)
    // =====================================================

    /**
     * Issue a machine access token for an authenticated {@link OAuthClient}.
     *
     * <p><strong>Additive — does NOT touch the existing {@code generateToken(UserDetails)}
     * path used by the legacy password grant.</strong></p>
     *
     * <p><strong>Effective authorities (Daraja 3 — RBAC + scope hybrid):</strong>
     * union of (a) permissions from bound roles and (b) the client's {@code scopes}
     * column. Optionally narrowed by the {@code requestedScope} parameter
     * (RFC 6749 §3.3 — space-delimited).</p>
     *
     * <p>JWT claims:</p>
     * <pre>
     * {
     *   "sub":              "&lt;client uuid&gt;",
     *   "typ":              "CLIENT",
     *   "username":         "&lt;client_id&gt;",
     *   "client_type":      "UNIVERSITY_BACKEND",
     *   "university_code":  "101",                          // if applicable
     *   "scope":            "students.view students.search", // space-delimited
     *   "authorities":      ["students.view", "students.search"]
     * }
     * </pre>
     *
     * @param client authenticated, operational OAuth client
     * @param requestedScope space-delimited subset of granted scopes — {@code null}
     *                       means "issue token for all granted authorities"
     * @return OAuth token response (no refresh_token — machines rotate via client_credentials)
     * @throws OAuthClientAuthenticationException with {@code invalid_scope} if a
     *         requested scope is not granted to the client
     */
    public TokenResponse issueClientToken(OAuthClient client, String requestedScope) {
        if (client == null) {
            throw new IllegalArgumentException("OAuthClient is required");
        }

        Instant now = Instant.now();
        // Machine token TTL = single ministry-wide policy (clientTokenExpirationSeconds; 24h default,
        // env OAUTH_CLIENT_TOKEN_EXPIRATION). System-to-system OTM callers reuse the token for its
        // lifetime, so a short TTL only forced needless re-requests. Sanity-clamp to [1min, 7day] so
        // a milliseconds-as-seconds misconfig can't mint an effectively non-expiring token. The
        // per-client oauth_client.access_token_ttl_seconds column is not consulted — the TTL is one
        // uniform policy; a per-OTM override can be reintroduced here later if a real need arises.
        int ttlSeconds = Math.min(Math.max(clientTokenExpirationSeconds, 60), 604800);
        Instant expiry = now.plusSeconds(ttlSeconds);

        // Effective authorities = role permissions ∪ direct scopes
        Set<String> effective = collectAuthorities(client);
        if (client.getScopes() != null) {
            effective.addAll(client.getScopes());
        }

        // Narrow by requested scope (RFC 6749 §3.3)
        Set<String> granted = narrowByRequestedScope(effective, requestedScope, client.getClientId());

        String universityCode = client.getUniversity() == null
                ? null
                : client.getUniversity().getCode();

        String scopeClaim = String.join(" ", new java.util.TreeSet<>(granted));
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())         // ✅ jti — unique token ID for blacklist
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(client.getId().toString())
                .claim("typ", SubjectType.CLIENT.name())
                .claim("username", client.getClientId())
                .claim("client_type", client.getClientType().name())
                .claim("scope", scopeClaim)
                .claim("authorities", granted.stream().sorted().toList());

        if (universityCode != null) {
            claims.claim("university_code", universityCode);
        }

        String accessToken = jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader(), claims.build()))
                .getTokenValue();

        // 'client_id' OAuth identifier hisoblanadi (sekret emas), lekin OTM-level audit'da
        // sezilarli — log.debug ga ko'chirildi (production debug profile'da yoqilsa ko'rinadi).
        log.debug("Issued CLIENT token for '{}' (type={}, univ={}, ttl={}s, authorities={}, scope={})",
                client.getClientId(),
                client.getClientType(),
                universityCode,
                ttlSeconds,
                granted.size(),
                scopeClaim);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("bearer")
                .expiresIn(ttlSeconds)
                .scope(scopeClaim)
                .build();
    }

    /**
     * Validate {@code requestedScope ⊆ effective} and return the narrowed authority set.
     *
     * <p>If {@code requestedScope} is {@code null}/blank, returns the full {@code effective}
     * set (default RFC 6749 behaviour — issue a token with the entire granted scope).</p>
     */
    private Set<String> narrowByRequestedScope(Set<String> effective,
                                               String requestedScope,
                                               String clientId) {
        if (requestedScope == null || requestedScope.isBlank()) {
            return effective;
        }
        Set<String> requested = Arrays.stream(requestedScope.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String s : requested) {
            if (!effective.contains(s)) {
                log.warn("client_credentials: client '{}' requested ungranted scope '{}'", clientId, s);
                throw OAuthClientAuthenticationException.invalidScope(
                        "Scope '" + s + "' is not granted to this client");
            }
        }
        return requested;
    }

    /**
     * Flatten an {@link OAuthClient}'s role → permission graph into granted authority strings.
     *
     * <p>Machine tokens carry authorities inline (no permission cache) because machines
     * come and go in bursts — a warm permission cache per client would churn.</p>
     */
    private Set<String> collectAuthorities(OAuthClient client) {
        Set<String> out = new HashSet<>();
        if (client.getRoles() == null) {
            return out;
        }
        client.getRoles().forEach(role -> {
            if (role == null || !role.isActive()) {
                return;
            }
            out.add("ROLE_" + role.getCode());
            if (role.getPermissions() == null) return;
            for (Permission p : role.getPermissions()) {
                if (p != null && p.getCode() != null) {
                    out.add(p.getCode());
                }
            }
        });
        return out;
    }

    // =====================================================
    // NOTE: JWT Tokens are Stateless
    // =====================================================
    // Unlike old-hemis (Redis/database sessions), JWT tokens are:
    // - Self-contained (all data in token)
    // - Stateless (no server-side storage needed)
    // - Verified by signature (HMAC-SHA256)
    //
    // Benefits:
    // ✅ No Redis/database dependency
    // ✅ Horizontally scalable (any server can verify)
    // ✅ Fast verification (no database lookup)
    //
    // Tradeoffs:
    // ⚠️  Cannot revoke before expiration (unless blacklist added)
    // ⚠️  Larger token size (vs UUID)
    // ⚠️  Payload visible (base64, not encrypted)
    //
    // Revocation Strategy (if needed):
    // 1. Short expiration time (12 hours)
    // 2. Refresh tokens (7 days)
    // 3. Token blacklist in Redis (optional)
    // 4. User logout invalidates refresh token
    // =====================================================
}
