package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.*;
import uz.hemis.security.service.UserPermissionCacheService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Web Frontend Authentication Controller
 *
 * <p><strong>New API v1 - Web Client Authentication</strong></p>
 *
 * <p>hemis-front (old-hemis yangi versiyasi) uchun JWT-based authentication</p>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>POST /api/v1/web/auth/login - Web login</li>
 *   <li>POST /api/v1/web/auth/logout - Web logout</li>
 *   <li>POST /api/v1/web/auth/refresh - Token refresh</li>
 *   <li>GET /api/v1/web/auth/me - Current user</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "Web Authentication v1", description = "hemis-front uchun authentication API")
@RestController
@RequestMapping("/api/v1/web/auth")
@RequiredArgsConstructor
@Slf4j
public class WebAuthController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserPermissionCacheService permissionCacheService;

    /**
     * Web Frontend Login
     *
     * <p><strong>Endpoint:</strong> POST /api/v1/web/auth/login</p>
     *
     * <p><strong>Request:</strong></p>
     * <pre>
     * POST /api/v1/web/auth/login
     * Content-Type: application/json
     *
     * {
     *   "username": "admin",
     *   "password": "admin123"
     * }
     * </pre>
     *
     * <p><strong>Response (OAuth2 Standard):</strong></p>
     * <pre>
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIs...",
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
     *   "tokenType": "Bearer",
     *   "expiresIn": 900
     * }
     * </pre>
     *
     * <p><strong>Best Practice:</strong></p>
     * <ul>
     *   <li>✅ NO user data (decode JWT on frontend)</li>
     *   <li>✅ NO permissions (backend checks on each request)</li>
     *   <li>✅ Clean OAuth2 format</li>
     *   <li>✅ expiresIn for token refresh timing</li>
     * </ul>
     */
    @Operation(
            summary = "Web login",
            description = "hemis-front (yangi old-hemis) uchun login endpoint. " +
                    "JWT token qaytaradi va localStorage'ga saqlanadi."
    )
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> credentials
    ) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("Web login attempt - username: {}", username);

        try {
            // Load user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Verify password using BCrypt
            log.debug("🔐 Password verification - Input: '{}', Hash from DB: '{}'",
                password, userDetails.getPassword().substring(0, Math.min(20, userDetails.getPassword().length())));

            boolean passwordMatches = passwordEncoder.matches(password, userDetails.getPassword());
            log.debug("🔐 Password match result: {}", passwordMatches);

            if (!passwordMatches) {
                log.error("Invalid password for user: {} (input='{}', hashStart='{}')",
                    username, password, userDetails.getPassword().substring(0, 20));
                return ResponseEntity.status(401).body(Map.of(
                        "error", "invalid_credentials",
                        "message", "Login yoki parol noto'g'ri"
                ));
            }

            // Generate JWT token
            Instant now = Instant.now();
            long expiresIn = 900L; // 15 minutes (best practice for access token)

            // ✅ MINIMAL JWT - No permissions, only essential claims
            JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
                    .issuer("hemis")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expiresIn))
                    .subject(userDetails.getUsername())
                    .build();

            JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, accessTokenClaims)).getTokenValue();

            // Refresh token
            long refreshExpiresIn = 604800L; // 7 days
            JwtClaimsSet refreshTokenClaims = JwtClaimsSet.builder()
                    .issuer("hemis")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(refreshExpiresIn))
                    .subject(userDetails.getUsername())
                    .claim("type", "refresh")
                    .build();

            String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, refreshTokenClaims)).getTokenValue();

            // ✅ Extract and cache permissions
            Set<String> permissions = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            // ✅ Cache permissions in Redis (TTL: 1 hour)
            permissionCacheService.cacheUserPermissions(username, permissions);

            // Build response (OAuth2 Standard - Clean & Minimal)
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", expiresIn); // Token expiration in seconds

            log.info("✅ Login successful - user: {}, cached {} permissions", username, permissions.size());

            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", username);
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_credentials",
                    "message", "Login yoki parol noto'g'ri"
            ));
        } catch (Exception e) {
            log.error("Web login failed - username: {}", username, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "internal_error",
                    "message", "Server xatoligi"
            ));
        }
    }

    /**
     * Web Logout
     *
     * <p><strong>Redis Cache Cleanup:</strong></p>
     * <ul>
     *   <li>Evicts user permissions from Redis cache</li>
     *   <li>Forces fresh permission load on next login</li>
     *   <li>JWT token remains valid until expiry (stateless design)</li>
     * </ul>
     */
    @Operation(
            summary = "Web logout",
            description = "Token'ni bekor qilish va Redis cache'ni tozalash"
    )
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Web logout request");

        // Extract username from Authorization header (JWT token)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Jwt jwt = jwtDecoder.decode(token);
                String username = jwt.getSubject();

                // ✅ Evict user permissions from Redis cache
                permissionCacheService.evictUserCache(username);
                log.info("✅ Evicted cache for user: {}", username);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Logged out successfully"
                ));

            } catch (Exception e) {
                log.warn("Failed to decode JWT token during logout: {}", e.getMessage());
                // Continue with logout even if token is invalid
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
        ));
    }

    /**
     * Refresh Token
     *
     * Best Practice Implementation:
     * - Validates refresh token signature and expiry
     * - Generates new access token (15 minutes)
     * - Rotates refresh token (7 days) for security
     */
    @Operation(
            summary = "Token yangilash",
            description = "Refresh token yordamida yangi access token olish"
    )
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestBody Map<String, String> body
    ) {
        String refreshToken = body.get("refreshToken");
        log.info("Web token refresh request");

        try {
            // Validate and decode refresh token
            Jwt decodedToken = jwtDecoder.decode(refreshToken);

            // Verify it's a refresh token (not access token)
            String tokenType = decodedToken.getClaimAsString("type");
            if (!"refresh".equals(tokenType)) {
                log.error("Invalid token type: {}", tokenType);
                return ResponseEntity.status(401).body(Map.of(
                        "error", "invalid_token",
                        "message", "Token turi noto'g'ri"
                ));
            }

            // Extract username from token
            String username = decodedToken.getSubject();
            if (username == null || username.isEmpty()) {
                log.error("No username in refresh token");
                return ResponseEntity.status(401).body(Map.of(
                        "error", "invalid_token",
                        "message", "Token'da foydalanuvchi ma'lumoti yo'q"
                ));
            }

            log.info("Refresh token valid for user: {}", username);

            // Generate new access token (15 minutes)
            Instant now = Instant.now();
            long expiresIn = 900L; // 15 minutes

            JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
                    .issuer("hemis")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expiresIn))
                    .subject(username)
                    .build();

            JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            String newAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, accessTokenClaims)).getTokenValue();

            // Generate new refresh token (7 days) - Token Rotation for security
            long refreshExpiresIn = 604800L; // 7 days
            JwtClaimsSet refreshTokenClaims = JwtClaimsSet.builder()
                    .issuer("hemis")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(refreshExpiresIn))
                    .subject(username)
                    .claim("type", "refresh")
                    .build();

            String newRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, refreshTokenClaims)).getTokenValue();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken); // Frontend expects 'accessToken'
            response.put("refreshToken", newRefreshToken);

            log.info("Token refreshed successfully for user: {}", username);

            return ResponseEntity.ok(response);

        } catch (JwtException e) {
            log.error("Invalid or expired refresh token: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_token",
                    "message", "Refresh token yaroqsiz yoki muddati o'tgan"
            ));
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "server_error",
                    "message", "Token yangilashda xatolik"
            ));
        }
    }

    /**
     * Get Current User
     */
    @Operation(
            summary = "Joriy foydalanuvchi",
            description = "Token'dan joriy foydalanuvchi ma'lumotlarini olish"
    )
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Get current user");
        // TODO: Token'dan user ma'lumotlarini olish
        return ResponseEntity.ok(Map.of(
                "username", "admin",
                "roles", new String[]{"USER", "ADMIN"}
        ));
    }
}
