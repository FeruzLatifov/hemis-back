package uz.hemis.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "LoginResponse",
    description = "JWT authentication response"
)
public class LoginResponse {

    @Schema(
        description = "JWT access token (expires in 15 minutes)"
    )
    private String accessToken;

    @Schema(
        description = "JWT refresh token (expires in 7 days)"
    )
    private String refreshToken;

    @Schema(
        description = "Token type (always 'Bearer')"
    )
    private String tokenType;

    @Schema(
        description = "Token expiration time in seconds (900 = 15 minutes)",
        minimum = "1"
    )
    private Long expiresIn;

    // ✅ Error fields (OAuth2 standard for error responses)
    @Schema(
        description = "Error code (OAuth2 standard: invalid_grant, too_many_requests, etc.)"
    )
    private String error;

    @Schema(
        description = "Human-readable error description"
    )
    private String errorDescription;
}
