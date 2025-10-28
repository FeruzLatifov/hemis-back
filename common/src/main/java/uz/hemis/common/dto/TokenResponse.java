package uz.hemis.common.dto;

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
 *   "access_token": "f1041fac-58cd-491a-a37d-212393a838f3",
 *   "token_type": "bearer",
 *   "refresh_token": "34583dda-9410-4410-95ff-cc0824656766",
 *   "expires_in": 43199,
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
     * <p>OLD-HEMIS default: 43199 seconds (12 hours - 1 second)</p>
     * <p>12 hours = 43200 seconds, but OLD-HEMIS uses 43199</p>
     */
    @JsonProperty("expires_in")
    @Builder.Default
    private Integer expiresIn = 43199;  // 12 hours (OLD-HEMIS compatibility)

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
