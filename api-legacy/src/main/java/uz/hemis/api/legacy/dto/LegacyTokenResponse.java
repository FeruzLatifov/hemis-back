package uz.hemis.api.legacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Legacy OAuth2 Token Response DTO
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Endpoint: POST /app/rest/oauth/token</li>
 *   <li>Endpoint: POST /app/rest/v2/oauth/token</li>
 *   <li>expires_in: 2591998 seconds (30 days - 2 seconds)</li>
 *   <li>Token format: Short base64 string</li>
 *   <li>Scope: "rest-api"</li>
 * </ul>
 *
 * <p><strong>Example Response from OLD-HEMIS:</strong></p>
 * <pre>
 * {
 *   "access_token": "p1HdTK8kXL-rl6RSWIovXOxR7-w",
 *   "token_type": "bearer",
 *   "refresh_token": "10DjfbHHclwKoVfPJmPnyMLbgJw",
 *   "expires_in": 2591998,
 *   "scope": "rest-api"
 * }
 * </pre>
 *
 * @since 1.0.0
 * @see uz.hemis.common.dto.TokenResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Legacy OAuth2 token response (old-hemis format)")
public class LegacyTokenResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Access token (short base64 string)
     *
     * <p>Example: "p1HdTK8kXL-rl6RSWIovXOxR7-w"</p>
     */
    @JsonProperty("access_token")
    @Schema(description = "Access token", example = "p1HdTK8kXL-rl6RSWIovXOxR7-w")
    private String accessToken;

    /**
     * Token type (always "bearer")
     */
    @JsonProperty("token_type")
    @Schema(description = "Token type", example = "bearer")
    @Builder.Default
    private String tokenType = "bearer";

    /**
     * Refresh token (short base64 string)
     *
     * <p>Example: "10DjfbHHclwKoVfPJmPnyMLbgJw"</p>
     */
    @JsonProperty("refresh_token")
    @Schema(description = "Refresh token", example = "10DjfbHHclwKoVfPJmPnyMLbgJw")
    private String refreshToken;

    /**
     * Token expiration in seconds
     *
     * <p><strong>OLD-HEMIS value: 2591998 seconds (30 days - 2 seconds)</strong></p>
     * <p>This matches the exact old-hemis response format.</p>
     */
    @JsonProperty("expires_in")
    @Schema(description = "Token expiration in seconds (30 days)", example = "2591998")
    @Builder.Default
    private Integer expiresIn = 2591998;  // 30 days - 2 seconds (OLD-HEMIS exact value)

    /**
     * Token scope (always "rest-api")
     */
    @JsonProperty("scope")
    @Schema(description = "Token scope", example = "rest-api")
    @Builder.Default
    private String scope = "rest-api";
}
