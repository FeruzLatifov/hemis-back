package uz.hemis.admin.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Login request DTO
 *
 * Validates user credentials for authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * Username (login)
     * 3-50 characters, required
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Password
     * 6-100 characters, required
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * Preferred locale for user session
     * Must be one of: uz, ru, en
     * Default: uz
     */
    @Pattern(regexp = "uz|ru|en", message = "Locale must be one of: uz, ru, en")
    @Builder.Default
    private String locale = "uz";
}
