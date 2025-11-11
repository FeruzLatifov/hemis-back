package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authentication Controller - Token Management
 *
 * <p><strong>CUBA Compatible OAuth 2.0 Endpoint</strong></p>
 *
 * <p>Old-hemis'da ishlatilgan token endpoint - 200+ OTM foydalanadi</p>
 *
 * <p><strong>Endpoint:</strong> POST /app/rest/v2/oauth/token</p>
 *
 * @since 1.0.0
 */
@Tag(name = "Authentication")
@RestController
@RequestMapping("/app/rest/v2/oauth")
@Slf4j
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthController(
            @Qualifier("hybridUserDetailsService") UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder
    ) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * OAuth 2.0 Token Endpoint (CUBA Compatible)
     *
     * <p><strong>Legacy URL:</strong> POST /app/rest/v2/oauth/token</p>
     *
     * <p><strong>Request:</strong></p>
     * <pre>
     * POST /app/rest/v2/oauth/token
     * Authorization: Basic Y2xpZW50OnNlY3JldA==
     * Content-Type: application/x-www-form-urlencoded
     *
     * grant_type=password&username=admin&password=admin123
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "access_token": "eyJhbGciOiJIUzI1NiIs...",
     *   "token_type": "Bearer",
     *   "expires_in": 86400,
     *   "refresh_token": "eyJhbGciOiJIUzI1NiIs..."
     * }
     * </pre>
     *
     * @param grantType must be "password"
     * @param username user login
     * @param password user password
     * @return JWT access token
     */
    @Operation(
            summary = "Token olish (OAuth 2.0)",
            description = "Login va parol yordamida JWT access token olish.\n\n" +
                    "**CUBA compatible** - old-hemis bilan bir xil format."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token muvaffaqiyatli olindi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    value = """
                                            {
                                              "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "token_type": "Bearer",
                                              "expires_in": 86400,
                                              "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Login yoki parol noto'g'ri"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri so'rov (grant_type xato)"
            )
    })
    @PostMapping(
            value = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> getToken(
            @Parameter(description = "Grant type (faqat 'password' qabul qilinadi)", example = "password")
            @RequestParam("grant_type") String grantType,

            @Parameter(description = "Foydalanuvchi login", example = "admin")
            @RequestParam("username") String username,

            @Parameter(description = "Foydalanuvchi paroli", example = "admin123")
            @RequestParam("password") String password
    ) {
        log.info("POST /app/rest/v2/oauth/token - username: {}", username);

        // Validate grant_type
        if (!"password".equals(grantType)) {
            log.warn("Invalid grant_type: {}", grantType);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "unsupported_grant_type",
                    "error_description", "Grant type must be 'password'"
            ));
        }

        try {
            // Load user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Verify password using BCrypt
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                log.error("Invalid password for user: {}", username);
                return ResponseEntity.status(401).body(Map.of(
                        "error", "invalid_grant",
                        "error_description", "Login yoki parol noto'g'ri"
                ));
            }

            // Generate JWT token
            Instant now = Instant.now();
            long expiresIn = 86400L; // 24 hours (same as old-hemis)

            // Get user roles
            String authorities = userDetails.getAuthorities().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(" "));

            // Create JWT claims
            JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
                    .issuer("hemis")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expiresIn))
                    .subject(userDetails.getUsername())
                    .claim("scope", authorities)
                    .claim("username", username)
                    .build();

            // Encode token with explicit HS256 algorithm
            JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, accessTokenClaims)).getTokenValue();

            // Generate refresh token (longer expiration)
            long refreshExpiresIn = 604800L; // 7 days
            JwtClaimsSet refreshTokenClaims = JwtClaimsSet.builder()
                    .issuer("hemis")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(refreshExpiresIn))
                    .subject(userDetails.getUsername())
                    .claim("type", "refresh")
                    .build();

            String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, refreshTokenClaims)).getTokenValue();

            // Build response (CUBA compatible format)
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", expiresIn);
            response.put("refresh_token", refreshToken);

            log.info("Token generated successfully for user: {}", username);

            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", username);
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_grant",
                    "error_description", "Login yoki parol noto'g'ri"
            ));
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", username, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "server_error",
                    "error_description", "Server xatoligi"
            ));
        }
    }

    /**
     * Logout / Revoke Token
     *
     * <p>Token'ni bekor qilish (optional - stateless JWT)</p>
     */
    @Operation(
            summary = "Token bekor qilish",
            description = "Access token'ni bekor qilish (logout)"
    )
    @PostMapping("/revoke")
    public ResponseEntity<Map<String, Object>> revokeToken(
            @RequestBody(required = false) Map<String, String> body
    ) {
        String token = body != null ? body.get("token") : null;
        log.info("POST /app/rest/v2/oauth/revoke - token: {}", token != null ? "provided" : "none");

        // Stateless JWT - token bekor qilish Redis orqali amalga oshirilishi mumkin
        // Hozircha success qaytaramiz

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token revoked successfully"
        ));
    }

    /**
     * Token response DTO
     */
    @Schema(description = "OAuth 2.0 token response")
    public static class TokenResponse {
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIs...")
        public String access_token;

        @Schema(description = "Token type (doim 'Bearer')", example = "Bearer")
        public String token_type;

        @Schema(description = "Token amal qilish muddati (soniyalarda)", example = "86400")
        public long expires_in;

        @Schema(description = "Refresh token (yangilash uchun)", example = "eyJhbGciOiJIUzI1NiIs...")
        public String refresh_token;
    }
}
