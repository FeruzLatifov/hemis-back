package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import uz.hemis.common.audit.AuditContext;
import uz.hemis.common.audit.LoginEvent;
import uz.hemis.common.port.security.UserIdentificationPort;
import uz.hemis.security.service.RateLimitService;
import uz.hemis.security.service.TokenBlacklistService;
import uz.hemis.security.service.UserPermissionCacheService;
import uz.hemis.web.dto.LoginRequest;
import uz.hemis.web.dto.LoginResponse;
import uz.hemis.web.dto.UserInfoResponse;
import uz.hemis.web.service.*;
import uz.hemis.web.service.WebAuthTokenService.TokenPair;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Web Frontend Authentication Controller
 *
 * <p>hemis-front (old-hemis yangi versiyasi) uchun JWT-based authentication.</p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>POST /api/v1/web/auth/login</li>
 *   <li>POST /api/v1/web/auth/logout</li>
 *   <li>POST /api/v1/web/auth/refresh</li>
 *   <li>GET  /api/v1/web/auth/me</li>
 *   <li>POST /api/v1/web/auth/cache/clear</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "Web Authentication v1", description = "hemis-front uchun authentication API")
@RestController
@RequestMapping("/api/v1/web/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class WebAuthController {

    private final AuthenticationManager authenticationManager;
    private final RateLimitService rateLimitService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserPermissionCacheService permissionCacheService;
    private final WebAuthTokenService tokenService;
    private final WebAuthCookieService cookieService;
    private final WebClientIpResolver clientIpResolver;
    private final WebAccountStatusValidator accountValidator;
    private final WebUserProfileService userProfileService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserIdentificationPort userIdentificationPort;

    // ==========================================================
    //  POST /login
    // ==========================================================

    @Operation(summary = "Web login", description = "hemis-front uchun login endpoint")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String username = request.getUsername();

        // Rate limit check
        String clientIp = clientIpResolver.resolve(httpRequest);
        if (!rateLimitService.isAllowed(clientIp)) {
            int remaining = (int) rateLimitService.getSecondsUntilReset(clientIp);
            log.warn("Rate limit exceeded for IP: {} (retry in {}s)", clientIp, remaining);
            return ResponseEntity.status(429).body(LoginResponse.builder()
                    .error("too_many_requests")
                    .errorDescription(String.format("Juda ko'p urinish. %d soniyadan keyin qayta urinib ko'ring.", remaining))
                    .build());
        }

        log.info("Web login attempt - username: {}, IP: {}", username, clientIp);

        try {
            // Authenticate via Spring Security
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            // Resolve userId (new or legacy account)
            UUID userId = accountValidator.resolveUserId(username);

            // Generate tokens — fullName JWT'ga snapshot sifatida o'rnatiladi (audit log uchun)
            String fullName = userIdentificationPort.getUserInfoByUsername(username)
                    .map(UserIdentificationPort.UserInfo::fullName)
                    .orElse(null);
            TokenPair tokens = tokenService.generateTokenPair(userId, username, fullName);

            // Cache permissions
            Set<String> permissions = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            permissionCacheService.cacheUserPermissions(userId, permissions);

            // Set HTTPOnly cookies (tokens NOT in response body)
            cookieService.setAuthCookies(httpResponse, tokens.accessToken(), tokens.refreshToken());

            // Reset failed attempts on successful login
            accountValidator.resetFailedAttempts(username);

            rateLimitService.reset(clientIp);
            log.info("Login successful - user: {}, {} permissions", username, permissions.size());

            eventPublisher.publishEvent(LoginEvent.builder()
                    .context(buildAuditContext(httpRequest, userId, username))
                    .eventType(LoginEvent.LoginEventType.LOGIN_SUCCESS)
                    .build());

            return ResponseEntity.ok(LoginResponse.builder()
                    .tokenType("Bearer")
                    .expiresIn(tokens.accessExpiresIn())
                    .build());

        } catch (DisabledException e) {
            log.warn("Login blocked - account disabled: {}", username);
            eventPublisher.publishEvent(LoginEvent.builder()
                    .context(buildAuditContext(httpRequest, null, username))
                    .eventType(LoginEvent.LoginEventType.LOGIN_FAILED)
                    .failureReason("Account disabled")
                    .build());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(LoginResponse.builder()
                    .error("account_disabled")
                    .errorDescription("Akkaunt faolsizlantirilgan. Administrator bilan bog'laning.")
                    .build());
        } catch (LockedException e) {
            log.warn("Login blocked - account locked: {}", username);
            eventPublisher.publishEvent(LoginEvent.builder()
                    .context(buildAuditContext(httpRequest, null, username))
                    .eventType(LoginEvent.LoginEventType.LOGIN_FAILED)
                    .failureReason("Account locked")
                    .build());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(LoginResponse.builder()
                    .error("account_locked")
                    .errorDescription("Akkaunt bloklangan. Administrator bilan bog'laning.")
                    .build());
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            // Record failed attempt and potentially lock account
            accountValidator.recordFailedAttempt(username);
            log.warn("Login failed - bad credentials: {}", username);
            eventPublisher.publishEvent(LoginEvent.builder()
                    .context(buildAuditContext(httpRequest, null, username))
                    .eventType(LoginEvent.LoginEventType.LOGIN_FAILED)
                    .failureReason("Bad credentials")
                    .build());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse.builder()
                    .error("invalid_credentials")
                    .errorDescription("Noto'g'ri foydalanuvchi nomi yoki parol.")
                    .build());
        } catch (Exception e) {
            log.error("Web login failed - username: {}", username, e);
            eventPublisher.publishEvent(LoginEvent.builder()
                    .context(buildAuditContext(httpRequest, null, username))
                    .eventType(LoginEvent.LoginEventType.LOGIN_FAILED)
                    .failureReason(e.getClass().getSimpleName() + ": " + e.getMessage())
                    .build());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse.builder()
                    .error("server_error")
                    .errorDescription("Server xatolik yuz berdi. Iltimos, qayta urinib ko'ring.")
                    .build());
        }
    }

    // ==========================================================
    //  POST /logout
    // ==========================================================

    @Operation(summary = "Web logout", description = "Token'ni bekor qilish va cache'ni tozalash")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("Web logout request");

        // Extract user info from tokens BEFORE blacklisting
        String accessToken = extractBearerToken(authHeader, accessTokenCookie);
        UUID logoutUserId = null;
        String logoutUsername = null;

        // 1) Access token dan olishga harakat qilish
        logoutUserId = extractUserIdFromToken(accessToken);
        logoutUsername = extractUsernameFromToken(accessToken);

        // 2) Refresh token dan fallback (access token da username bo'lmasligi mumkin)
        if (logoutUsername == null && refreshTokenCookie != null) {
            logoutUsername = extractUsernameFromToken(refreshTokenCookie);
        }
        if (logoutUserId == null && refreshTokenCookie != null) {
            logoutUserId = extractUserIdFromToken(refreshTokenCookie);
        }

        // Now blacklist tokens (after extracting user info)
        if (accessToken != null) {
            blacklistToken(accessToken, true);
        }
        if (refreshTokenCookie != null) {
            blacklistToken(refreshTokenCookie, false);
        }

        // Clear cookies
        cookieService.clearAuthCookies(httpResponse);
        log.info("Logout successful for user: {}", logoutUsername);

        eventPublisher.publishEvent(LoginEvent.builder()
                .context(buildAuditContext(httpRequest, logoutUserId,
                        logoutUsername != null ? logoutUsername : "unknown"))
                .eventType(LoginEvent.LoginEventType.LOGOUT)
                .build());

        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    // ==========================================================
    //  POST /refresh
    // ==========================================================

    @Operation(summary = "Token yangilash", description = "Refresh token yordamida yangi access token olish")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestBody(required = false) Map<String, String> body,
            @CookieValue(value = "refreshToken", required = false) String cookieRefreshToken,
            HttpServletResponse httpResponse
    ) {
        String refreshToken = resolveRefreshToken(body, cookieRefreshToken);
        if (refreshToken == null) {
            return unauthorized("invalid_request", "Refresh token talab qilinadi");
        }

        log.info("Web token refresh request");

        try {
            Jwt decoded = tokenService.decode(refreshToken);

            // Verify token type
            if (!"refresh".equals(decoded.getClaimAsString("type"))) {
                return unauthorized("invalid_token", "Token turi noto'g'ri");
            }

            // Check blacklist
            String oldJti = decoded.getId();
            if (oldJti != null && tokenBlacklistService.isBlacklisted(oldJti)) {
                log.warn("Attempt to use blacklisted refresh token: {}", oldJti);
                return unauthorized("invalid_token", "Token bekor qilingan (logout qilingan)");
            }

            // Parse userId
            UUID userId = parseUserId(decoded.getSubject());
            if (userId == null) {
                return unauthorized("invalid_token", "Token'da noto'g'ri foydalanuvchi ID");
            }

            // Validate account is still active
            accountValidator.validateForRefresh(userId, decoded);

            // Generate new token pair — refresh paytida fullName ni qayta o'qiymiz (snapshot)
            String username = decoded.getClaimAsString("username");
            String fullName = username != null
                    ? userIdentificationPort.getUserInfoByUsername(username)
                            .map(UserIdentificationPort.UserInfo::fullName)
                            .orElse(null)
                    : null;
            TokenPair tokens = tokenService.generateTokenPair(userId, username, fullName);

            // Set new cookies
            cookieService.setAuthCookies(httpResponse, tokens.accessToken(), tokens.refreshToken());

            // Blacklist old refresh token (rotation)
            if (oldJti != null && decoded.getExpiresAt() != null) {
                tokenBlacklistService.addToBlacklist(oldJti, decoded.getExpiresAt());
                log.info("Old refresh token blacklisted: {}", oldJti);
            }

            log.debug("Token refreshed for userId: {}", userId);

            return ResponseEntity.ok(Map.of("success", true, "message", "Tokens refreshed"));

        } catch (WebAccountStatusValidator.AccountDisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "account_disabled", "message", e.getMessage()));
        } catch (WebAccountStatusValidator.AccountLockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "account_locked", "message", e.getMessage()));
        } catch (WebAccountStatusValidator.CredentialsExpiredException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "credentials_expired", "message", e.getMessage()));
        } catch (WebAccountStatusValidator.InvalidTokenException e) {
            return unauthorized("invalid_token", e.getMessage());
        } catch (JwtException e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return unauthorized("invalid_token", "Refresh token yaroqsiz yoki muddati o'tgan");
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "server_error", "message", "Token yangilashda xatolik"));
        }
    }

    // ==========================================================
    //  GET /me
    // ==========================================================

    @Operation(summary = "Get current user info", description = "JWT orqali joriy foydalanuvchi ma'lumotlari")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User info",
                content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = parseUserIdOrThrow(jwt.getSubject());
        log.info("/auth/me request for userId: {}", userId);

        UserInfoResponse profile = userProfileService.loadCurrentUser(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(
                CacheControl.maxAge(60, TimeUnit.SECONDS)
                        .cachePrivate()
                        .staleWhileRevalidate(120, TimeUnit.SECONDS));

        return ResponseEntity.ok().headers(headers).body(profile);
    }

    // ==========================================================
    //  POST /cache/clear
    // ==========================================================

    @Operation(summary = "Clear user cache", description = "Joriy foydalanuvchi keshini tozalash")
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearUserCache(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = parseUserIdOrThrow(jwt.getSubject());
        log.info("POST /cache/clear for userId: {}", userId);

        List<String> cleared = userProfileService.clearUserCache(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cache cleared successfully",
                "cleared", cleared));
    }

    // ==========================================================
    //  Private helpers
    // ==========================================================

    private String extractBearerToken(String authHeader, String cookieToken) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return cookieToken;
    }

    private void blacklistToken(String rawToken, boolean evictCache) {
        try {
            Jwt jwt = tokenService.decode(rawToken);
            String jti = jwt.getId();
            Instant exp = jwt.getExpiresAt();
            if (jti != null && exp != null) {
                tokenBlacklistService.addToBlacklist(jti, exp);
                log.info("Token blacklisted: jti={}", jti);
            }
            if (evictCache) {
                try {
                    UUID userId = UUID.fromString(jwt.getSubject());
                    permissionCacheService.evictUserCache(userId);
                } catch (IllegalArgumentException ignored) { }
            }
        } catch (Exception e) {
            log.warn("Failed to decode token during logout: {}", e.getMessage());
        }
    }

    private String resolveRefreshToken(Map<String, String> body, String cookieToken) {
        if (body != null && body.get("refreshToken") != null) {
            return body.get("refreshToken");
        }
        return cookieToken;
    }

    private UUID parseUserId(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            log.error("Invalid userId in token: {}", subject);
            return null;
        }
    }

    private UUID parseUserIdOrThrow(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid user ID in token");
        }
    }

    private ResponseEntity<Map<String, Object>> unauthorized(String error, String message) {
        return ResponseEntity.status(401).body(Map.of("error", error, "message", message));
    }

    /**
     * JWT dan username olish — avval decode, keyin Base64 fallback (expired token uchun).
     */
    private String extractUsernameFromToken(String token) {
        if (token == null) return null;
        try {
            Jwt jwt = tokenService.decode(token);
            return jwt.getClaimAsString("username");
        } catch (Exception e) {
            return parseJwtPayloadClaim(token, "username");
        }
    }

    /**
     * JWT dan userId olish — avval decode, keyin Base64 fallback (expired token uchun).
     */
    private UUID extractUserIdFromToken(String token) {
        if (token == null) return null;
        try {
            Jwt jwt = tokenService.decode(token);
            return parseUserId(jwt.getSubject());
        } catch (Exception e) {
            String sub = parseJwtPayloadClaim(token, "sub");
            return sub != null ? parseUserId(sub) : null;
        }
    }

    /**
     * JWT payload ni Base64 decode qilib claim olish (validation siz — expired/invalid token uchun).
     */
    private String parseJwtPayloadClaim(String token, String claim) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                com.fasterxml.jackson.databind.JsonNode json =
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
                return json.has(claim) ? json.get(claim).asText() : null;
            }
        } catch (Exception e) {
            // JWT parse xatosi — debug log'ga yoziladi, client null oladi
            log.debug("Failed to parse JWT claim: {}", e.getMessage());
        }
        return null;
    }

    private AuditContext buildAuditContext(HttpServletRequest request, UUID userId, String username) {
        AuditContext.AuditContextBuilder builder = AuditContext.builder()
                .userId(userId)
                .username(username)
                .requestId(MDC.get("requestId"));

        if (request != null) {
            String resolvedIp = clientIpResolver.resolve(request);
            log.info("Audit IP: resolved={}, remoteAddr={}, X-Forwarded-For={}, X-Real-IP={}",
                    resolvedIp, request.getRemoteAddr(),
                    request.getHeader("X-Forwarded-For"), request.getHeader("X-Real-IP"));
            builder.ip(resolvedIp)
                   .userAgent(request.getHeader("User-Agent"))
                   .endpoint(request.getRequestURI());
        }

        return builder.build();
    }
}
