package uz.hemis.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.hemis.admin.dto.auth.*;
import uz.hemis.admin.dto.user.AdminUserDTO;
import uz.hemis.admin.service.AuthenticationService;

/**
 * Authentication Controller
 *
 * REST endpoints for user authentication
 * Base path: /api/v1/admin/auth
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * User login
     *
     * POST /api/v1/admin/auth/login
     *
     * @param request Login credentials
     * @return JWT tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - username: {}", request.getUsername());

        LoginResponse response = authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     *
     * POST /api/v1/admin/auth/refresh
     *
     * @param request Refresh token request
     * @return New token pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /auth/refresh");

        LoginResponse response = authenticationService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }

    /**
     * User logout
     *
     * POST /api/v1/admin/auth/logout
     * Requires: Authorization Bearer token
     *
     * @param userId Current user ID from JWT token
     * @return Success response
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal String userId) {
        log.info("POST /auth/logout - userId: {}", userId);

        authenticationService.logout(userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Get current authenticated user
     *
     * GET /api/v1/admin/auth/me
     * Requires: Authorization Bearer token
     *
     * @param userId Current user ID from JWT token
     * @return Current user info
     */
    @GetMapping("/me")
    public ResponseEntity<AdminUserDTO> getCurrentUser(@AuthenticationPrincipal String userId) {
        log.info("GET /auth/me - userId: {}", userId);

        AdminUserDTO user = authenticationService.getCurrentUser(userId);

        return ResponseEntity.ok(user);
    }

    /**
     * Validate token
     *
     * GET /api/v1/admin/auth/validate
     * Requires: Authorization Bearer token
     *
     * @param userId Current user ID from JWT token
     * @return Validation result
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@AuthenticationPrincipal String userId) {
        log.info("GET /auth/validate - userId: {}", userId);

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .userId(userId)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Token validation response DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TokenValidationResponse {
        private boolean valid;
        private String userId;
    }
}
