package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.security.service.RateLimitService;
import uz.hemis.service.auth.PasswordResetService;

@RestController
@RequestMapping("/api/v1/web/auth")
@Tag(name = "Password Reset", description = "Forgot password and reset password endpoints")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final RateLimitService rateLimitService;

    // OWASP Auth Cheat Sheet: 5 attempts / 15 min (forgot), 10 / 1 hour (reset).
    private static final String FORGOT_PREFIX = "ratelimit:forgot-password:";
    private static final String RESET_PREFIX = "ratelimit:reset-password:";

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends a password reset link to the user's email. Always returns 200 for security. Per-IP + per-email rate limit applied.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request processed (email sent if user exists)"),
            @ApiResponse(responseCode = "429", description = "Too many requests — rate limit exceeded")
    })
    public ResponseEntity<ResponseWrapper<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        // Per-IP (anti-spam) + per-email (anti-enumeration) — fail closed if Redis down.
        String ip = resolveClientIp(httpRequest);
        if (!rateLimitService.isAllowed(FORGOT_PREFIX + "ip:", ip, 5, 15)
                || !rateLimitService.isAllowed(FORGOT_PREFIX + "email:", request.getEmail(), 5, 60)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ResponseWrapper.error("Too many requests. Try again later."));
        }
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(ResponseWrapper.success(null, "If the email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token", description = "Resets password using a valid reset token from email. Per-IP rate limit (anti-brute-force).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid/expired token or password validation failed"),
            @ApiResponse(responseCode = "429", description = "Too many requests — rate limit exceeded")
    })
    public ResponseEntity<ResponseWrapper<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = resolveClientIp(httpRequest);
        if (!rateLimitService.isAllowed(RESET_PREFIX + "ip:", ip, 10, 60)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ResponseWrapper.error("Too many requests. Try again later."));
        }
        passwordResetService.resetPassword(request.getToken(), request.getPassword());
        return ResponseEntity.ok(ResponseWrapper.success(null, "Password has been reset successfully."));
    }

    /** Extract client IP, honoring proxy headers when present. */
    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // ── Request DTOs ──

    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "Token is required")
        private String token;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).+$",
                message = "Password must contain uppercase, lowercase, digit, and special character")
        private String password;
    }
}
