package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Admin Authentication Controller
 *
 * Admin panel login endpoint - old-hemis `/app/#login` ekvivalenti
 */
@Tag(name = "Admin Authentication")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    /**
     * Admin Login
     *
     * old-hemis: /app/#login (Vaadin UI)
     * hemis-back: /api/admin/login (JSON API)
     */
    @Operation(
            summary = "Admin panel login",
            description = "Admin foydalanuvchilar uchun login (old-hemis /app/#login ekvivalenti)"
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

        log.info("Admin login attempt - username: {}", username);

        try {
            // Load user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Verify password using BCrypt
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                log.error("Invalid password for user: {}", username);
                return ResponseEntity.status(401).body(Map.of(
                        "error", "invalid_credentials",
                        "message", "Login yoki parol noto'g'ri"
                ));
            }

            // Generate JWT token
            Instant now = Instant.now();
            long expiresIn = 86400L; // 24 hours

            String authorities = userDetails.getAuthorities().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(" "));

            JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
                    .issuer("hemis")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expiresIn))
                    .subject(userDetails.getUsername())
                    .claim("scope", authorities)
                    .claim("username", username)
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

            // Build user object
            Map<String, Object> user = new HashMap<>();
            user.put("id", username); // For now, use username as ID
            user.put("username", username);
            user.put("email", username + "@hemis.uz"); // Default email
            user.put("name", username);
            user.put("locale", "uz");
            user.put("active", true);
            user.put("createdAt", now.toString());

            // Build response (Frontend compatible format)
            Map<String, Object> response = new HashMap<>();
            response.put("token", accessToken); // Frontend expects 'token' not 'access_token'
            response.put("refreshToken", refreshToken); // Frontend expects 'refreshToken' not 'refresh_token'
            response.put("user", user);
            response.put("university", null); // System admin has no university
            response.put("permissions", new String[]{}); // Empty permissions for now

            log.info("Admin login successful - username: {}", username);

            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", username);
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_credentials",
                    "message", "Login yoki parol noto'g'ri"
            ));
        } catch (Exception e) {
            log.error("Admin login failed - username: {}", username, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "internal_error",
                    "message", "Server xatoligi"
            ));
        }
    }

    /**
     * Admin Logout
     */
    @Operation(
            summary = "Admin logout",
            description = "Admin token'ni bekor qilish"
    )
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        log.info("Admin logout");
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
        ));
    }

    /**
     * Get Current User
     */
    @Operation(
            summary = "Get current admin user",
            description = "Joriy admin foydalanuvchi ma'lumotlari"
    )
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Get current admin user");
        // TODO: Token'dan user ma'lumotlarini olish
        return ResponseEntity.ok(Map.of(
                "username", "admin",
                "roles", new String[]{"ADMIN"}
        ));
    }
}
