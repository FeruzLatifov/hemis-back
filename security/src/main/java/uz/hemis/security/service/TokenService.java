package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.TokenResponse;

import java.time.Instant;
import java.util.Set;
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
    private final uz.hemis.domain.repository.UserRepository userRepository;
    private final uz.hemis.domain.repository.SecUserRepository secUserRepository;  // ✅ OLD-HEMIS fallback

    @Value("${hemis.security.jwt.expiration:2592000}")  // 30 days (2592000 seconds) - OLD-HEMIS compatible
    private long accessTokenValiditySeconds;

    @Value("${hemis.security.jwt.refresh-expiration:604800}")  // 7 days
    private long refreshTokenValiditySeconds;

    @Value("${hemis.security.jwt.issuer:hemis-backend}")
    private String issuer;

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
        log.info("Generating JWT token for user: {}", userDetails.getUsername());

        // ✅ HYBRID: Try users table first, fallback to sec_user
        String userId = getUserId(userDetails.getUsername());

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

        // Build JWS Header with explicit HS256 algorithm
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // ✅ MINIMAL JWT - No permissions, only essential claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)                          // Issuer: hemis-backend
                .issuedAt(now)                            // Issued at: now
                .expiresAt(expiry)                        // Expires at: now + 30 days
                .subject(userId)                          // ✅ Subject: userId (UUID string)
                .claim("username", userDetails.getUsername())  // Username claim (for display)
                .claim("scope", "rest-api")              // Scope claim (OAuth2)
                .build();

        // Encode JWT with explicit headers (algorithm must match JWK)
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        // ✅ Generate refresh token (old-hemis compatibility)
        String refreshToken = generateRefreshToken(userDetails);

        log.info("JWT token generated for user: {} (expires in {} seconds)",
                userDetails.getUsername(), accessTokenValiditySeconds);

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
        log.info("Generating refresh token for user: {}", userDetails.getUsername());

        // ✅ HYBRID: Try users table first, fallback to sec_user
        String userId = getUserId(userDetails.getUsername());

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenValiditySeconds);

        // Build JWS Header with explicit HS256 algorithm
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // ✅ MINIMAL JWT - No permissions, only essential claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(userId)                          // ✅ Subject: userId (UUID string)
                .claim("username", userDetails.getUsername())  // Username (for display)
                .claim("type", "refresh")  // Mark as refresh token
                .build();

        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

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

            // Extract userId from JWT 'sub' claim
            String userIdString = jwt.getSubject();

            if (userIdString == null || userIdString.isEmpty()) {
                throw new IllegalArgumentException("UserId not found in refresh token");
            }

            java.util.UUID userId = java.util.UUID.fromString(userIdString);
            String username = jwt.getClaimAsString("username");  // Optional (for logging)

            log.debug("Refreshing token for userId: {} (username: {})", userId, username);

            // Generate new tokens directly from claims (stateless approach)
            Instant now = Instant.now();
            Instant accessExpiry = now.plusSeconds(accessTokenValiditySeconds);
            Instant refreshExpiry = now.plusSeconds(refreshTokenValiditySeconds);

            // Build JWS Header with explicit HS256 algorithm
            JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

            // ✅ MINIMAL JWT - No permissions, only essential claims
            JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(accessExpiry)
                    .subject(userId.toString())  // ✅ userId (UUID)
                    .claim("username", username)  // Username (for display)
                    .claim("scope", "rest-api")
                    .build();

            String newAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, accessClaims)).getTokenValue();

            // ✅ MINIMAL JWT - No permissions, only essential claims
            JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(refreshExpiry)
                    .subject(userId.toString())  // ✅ userId (UUID)
                    .claim("username", username)  // Username (for display)
                    .claim("type", "refresh")
                    .build();

            String newRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, refreshClaims)).getTokenValue();

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
     * Get userId by username - HYBRID lookup (users → sec_user fallback)
     *
     * <p><strong>CRITICAL - sec_user Compatibility:</strong></p>
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
        // 1️⃣ Try users table first (new structure)
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            String userId = userOpt.get().getId().toString();
            log.debug("✅ User found in 'users' table: {} -> {}", username, userId);
            return userId;
        }

        // 2️⃣ Fallback to sec_user table (old-hemis)
        var secUserOpt = secUserRepository.findByLogin(username);
        if (secUserOpt.isPresent()) {
            String userId = secUserOpt.get().getId().toString();
            log.debug("✅ User found in 'sec_user' table (fallback): {} -> {}", username, userId);
            return userId;
        }

        // 3️⃣ User not found in either table
        log.error("❌ User not found in 'users' or 'sec_user' tables: {}", username);
        throw new IllegalArgumentException("User not found: " + username);
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
