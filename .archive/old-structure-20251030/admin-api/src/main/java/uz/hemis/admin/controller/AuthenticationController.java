package uz.hemis.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Base path: /app/rest/v2/auth
 */
@Tag(name = "Authentication", description = "Admin panel authentication endpoints (JWT-based)")
@Slf4j
@RestController
@RequestMapping("/app/rest/v2/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * User login
     *
     * POST /app/rest/v2/auth/login
     *
     * @param request Login credentials
     * @return JWT tokens and user info
     */
    @Operation(
            summary = "Admin panel login",
            description = "Authenticate admin user with username/password and get JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Login credentials (username and password)")
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - username: {}", request.getUsername());

        LoginResponse response = authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     *
     * POST /app/rest/v2/auth/refresh
     *
     * @param request Refresh token request
     * @return New token pair
     */
    @Operation(
            summary = "Refresh JWT token",
            description = "Get new access token using refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Parameter(description = "Refresh token")
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /auth/refresh");

        LoginResponse response = authenticationService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }

    /**
     * User logout
     *
     * POST /app/rest/v2/auth/logout
     * Requires: Authorization Bearer token
     *
     * @param userId Current user ID from JWT token
     * @return Success response
     */
    @Operation(
            summary = "Logout",
            description = "Logout current user (invalidate token)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userId) {
        log.info("POST /auth/logout - userId: {}", userId);

        authenticationService.logout(userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Get current authenticated user
     *
     * GET /app/rest/v2/auth/me
     * Requires: Authorization Bearer token
     *
     * @param userId Current user ID from JWT token
     * @return Current user info
     */
    @Operation(
            summary = "Get current user",
            description = "Get authenticated user information and permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User info retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AdminUserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<AdminUserDTO> getCurrentUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userId) {
        log.info("GET /auth/me - userId: {}", userId);

        AdminUserDTO user = authenticationService.getCurrentUser(userId);

        return ResponseEntity.ok(user);
    }

    /**
     * Validate token
     *
     * GET /app/rest/v2/auth/validate
     * Requires: Authorization Bearer token
     *
     * @param userId Current user ID from JWT token
     * @return Validation result
     */
    @Operation(
            summary = "Validate JWT token",
            description = "Check if current JWT token is valid"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token is valid",
                    content = @Content(schema = @Schema(implementation = TokenValidationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token is invalid or expired"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userId) {
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
