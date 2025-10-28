package uz.hemis.admin.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Refresh token request DTO
 *
 * Used to obtain new access token using refresh token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    /**
     * Refresh token obtained from login
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
