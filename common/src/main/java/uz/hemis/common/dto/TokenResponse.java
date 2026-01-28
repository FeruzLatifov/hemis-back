package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * OAuth2 Token Response DTO
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Field names match OLD-HEMIS OAuth2 response EXACTLY</li>
 *   <li>Uses snake_case (access_token, not accessToken)</li>
 *   <li>Token type is always "bearer"</li>
 *   <li>Scope is always "rest-api"</li>
 * </ul>
 *
 * <p><strong>Example Response:</strong></p>
 * <pre>
 * {
 *   "access_token": "Qj4ndwMJ76yu6xxFPYwyxzdUN6c",
 *   "token_type": "bearer",
 *   "refresh_token": "dAOjVT_EEsfr34RALHakaADX0k0",
 *   "expires_in": 2591998,
 *   "scope": "rest-api"
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // ✅ Don't include null fields (old-hemis compatibility)
public class TokenResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Access token (UUID format)
     * JSON field: "access_token"
     *
     * <p>Example: "f1041fac-58cd-491a-a37d-212393a838f3"</p>
     * <p>Stored in Redis with user details</p>
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Token type (always "bearer")
     * JSON field: "token_type"
     */
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "bearer";

    /**
     * Refresh token (UUID format)
     * JSON field: "refresh_token"
     *
     * <p>Example: "34583dda-9410-4410-95ff-cc0824656766"</p>
     * <p>Used to get new access token when expired</p>
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * Token expiration in seconds
     * JSON field: "expires_in"
     *
     * <p>OLD-HEMIS default: 2591998 seconds (30 days - 2 seconds)</p>
     * <p>30 days = 2592000 seconds, but OLD-HEMIS uses 2591998</p>
     */
    @JsonProperty("expires_in")
    @Builder.Default
    private Integer expiresIn = 2591998;  // 30 days (OLD-HEMIS compatibility)

    /**
     * Token scope (always "rest-api")
     * JSON field: "scope"
     *
     * <p>OLD-HEMIS uses "rest-api" scope for all tokens</p>
     */
    @JsonProperty("scope")
    @Builder.Default
    private String scope = "rest-api";
}
