package uz.hemis.api.web.dto;

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
    description = "JWT authentication response",
    example = """
        {
          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6ImhlbWlzIiwiaWF0IjoxNzA1MDYyMDAwLCJleHAiOjE3MDUwNjI5MDB9.abc123",
          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6ImhlbWlzIiwiaWF0IjoxNzA1MDYyMDAwLCJleHAiOjE3MDU2NjY4MDB9.xyz789",
          "tokenType": "Bearer",
          "expiresIn": 900
        }
        """
)
public class LoginResponse {

    @Schema(
        description = "JWT access token (expires in 15 minutes)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6ImhlbWlzIiwiaWF0IjoxNzA1MDYyMDAwLCJleHAiOjE3MDUwNjI5MDB9.abc123"
    )
    private String accessToken;

    @Schema(
        description = "JWT refresh token (expires in 7 days)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6ImhlbWlzIiwiaWF0IjoxNzA1MDYyMDAwLCJleHAiOjE3MDU2NjY4MDB9.xyz789"
    )
    private String refreshToken;

    @Schema(
        description = "Token type (always 'Bearer')",
        example = "Bearer"
    )
    private String tokenType;

    @Schema(
        description = "Token expiration time in seconds (900 = 15 minutes)",
        example = "900",
        minimum = "1"
    )
    private Long expiresIn;

    // ✅ Error fields (OAuth2 standard for error responses)
    @Schema(
        description = "Error code (OAuth2 standard: invalid_grant, too_many_requests, etc.)",
        example = "too_many_requests"
    )
    private String error;

    @Schema(
        description = "Human-readable error description",
        example = "Too many login attempts. Please try again in 10 minutes."
    )
    private String errorDescription;
}

