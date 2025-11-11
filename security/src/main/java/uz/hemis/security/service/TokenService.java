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

    @Value("${hemis.security.jwt.expiration:43200}")  // 12 hours (43200 seconds)
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
     *   "token_type": "Bearer",
     *   "expires_in": 43200,
     *   "scope": "rest-api"
     * }
     * </pre>
     *
     * @param userDetails authenticated user (from sec_user)
     * @return TokenResponse containing JWT access token
     */
    public TokenResponse generateToken(UserDetails userDetails) {
        log.info("Generating JWT token for user: {}", userDetails.getUsername());

        // Current time
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenValiditySeconds);

        // Extract roles from authorities
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        log.debug("User {} has roles: {}", userDetails.getUsername(), roles);

        // Build JWS Header with explicit HS256 algorithm
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // Build JWT claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)                          // Issuer: hemis-backend
                .issuedAt(now)                            // Issued at: now
                .expiresAt(expiry)                        // Expires at: now + 12h
                .subject(userDetails.getUsername())      // Subject: username
                .claim("username", userDetails.getUsername())  // Username claim
                .claim("roles", roles)                    // Roles claim (comma-separated)
                .claim("scope", "rest-api")              // Scope claim (OAuth2)
                .build();

        // Encode JWT with explicit headers (algorithm must match JWK)
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        log.info("JWT token generated for user: {} (expires in {} seconds)",
                userDetails.getUsername(), accessTokenValiditySeconds);

        // Build OAuth2 token response
        return TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")  // OAuth2 standard: "Bearer" (capital B)
                .expiresIn((int) accessTokenValiditySeconds)
                .scope("rest-api")
                .build();
    }

    /**
     * Generate refresh token (future implementation)
     *
     * <p><strong>NOT IMPLEMENTED YET:</strong></p>
     * <ul>
     *   <li>JWT refresh tokens require different expiration</li>
     *   <li>Should be stored separately or have different signature</li>
     *   <li>For now, clients must re-authenticate after access token expires</li>
     * </ul>
     *
     * @param userDetails user details
     * @return refresh token (same structure as access token for now)
     */
    public String generateRefreshToken(UserDetails userDetails) {
        log.info("Generating refresh token for user: {}", userDetails.getUsername());

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenValiditySeconds);

        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Build JWS Header with explicit HS256 algorithm
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(userDetails.getUsername())
                .claim("username", userDetails.getUsername())
                .claim("roles", roles)
                .claim("type", "refresh")  // Mark as refresh token
                .build();

        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        log.info("Refresh token generated for user: {} (expires in {} days)",
                userDetails.getUsername(), refreshTokenValiditySeconds / 86400);

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

            // Extract user information from JWT claims
            String username = jwt.getClaimAsString("username");
            String roles = jwt.getClaimAsString("roles");

            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username not found in refresh token");
            }

            log.debug("Refreshing token for user: {} with roles: {}", username, roles);

            // Generate new tokens directly from claims (stateless approach)
            Instant now = Instant.now();
            Instant accessExpiry = now.plusSeconds(accessTokenValiditySeconds);
            Instant refreshExpiry = now.plusSeconds(refreshTokenValiditySeconds);

            // Build JWS Header with explicit HS256 algorithm
            JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

            // Build new access token
            JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(accessExpiry)
                    .subject(username)
                    .claim("username", username)
                    .claim("roles", roles)
                    .claim("scope", "rest-api")
                    .build();

            String newAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, accessClaims)).getTokenValue();

            // Build new refresh token
            JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(refreshExpiry)
                    .subject(username)
                    .claim("username", username)
                    .claim("roles", roles)
                    .claim("type", "refresh")
                    .build();

            String newRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, refreshClaims)).getTokenValue();

            // Build response
            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn((int) accessTokenValiditySeconds)
                    .scope("rest-api")
                    .build();

            log.info("Access token refreshed successfully for user: {}", username);

            return tokenResponse;

        } catch (JwtException e) {
            log.warn("Invalid or expired refresh token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired refresh token", e);
        }
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
