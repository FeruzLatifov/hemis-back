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
 * Legacy User Info Response DTO
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Endpoint: GET /app/rest/user/info</li>
 *   <li>Wrapped in ResponseWrapper with success, status, statusText, responseTime</li>
 *   <li>User data in "data" field</li>
 * </ul>
 *
 * <p><strong>Example Response from OLD-HEMIS:</strong></p>
 * <pre>
 * {
 *   "success": true,
 *   "status": 200,
 *   "statusText": "",
 *   "responseTime": 90,
 *   "data": {
 *     "id": "00000000-0000-0000-0000-000000000000",
 *     "login": "username",
 *     "name": "User Full Name",
 *     "firstName": "User",
 *     "middleName": "Middle",
 *     "lastName": "Name",
 *     "position": "Position",
 *     "email": "user@example.com",
 *     "timeZone": "Asia/Tashkent",
 *     "language": "ru",
 *     "_instanceName": "User [username]",
 *     "locale": "ru"
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)  // Include null fields for old-hemis compatibility
@Schema(description = "Legacy user info response wrapper (old-hemis format)")
public class LegacyUserInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("success")
    @Schema(description = "Success flag", example = "true")
    @Builder.Default
    private Boolean success = true;

    @JsonProperty("status")
    @Schema(description = "HTTP status code", example = "200")
    @Builder.Default
    private Integer status = 200;

    @JsonProperty("statusText")
    @Schema(description = "Status text", example = "")
    @Builder.Default
    private String statusText = "";

    @JsonProperty("responseTime")
    @Schema(description = "Response time in milliseconds", example = "90")
    private Long responseTime;

    @JsonProperty("data")
    @Schema(description = "User data")
    private UserData data;

    /**
     * Nested user data object
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)  // Include null fields
    @Schema(description = "User data")
    public static class UserData implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("id")
        @Schema(description = "User ID (UUID)", example = "00000000-0000-0000-0000-000000000000")
        private String id;

        @JsonProperty("login")
        @Schema(description = "User login", example = "username")
        private String login;

        @JsonProperty("name")
        @Schema(description = "Full name", example = "User Full Name")
        private String name;

        @JsonProperty("firstName")
        @Schema(description = "First name", example = "User")
        private String firstName;

        @JsonProperty("middleName")
        @Schema(description = "Middle name", example = "null")
        private String middleName;

        @JsonProperty("lastName")
        @Schema(description = "Last name", example = "null")
        private String lastName;

        @JsonProperty("position")
        @Schema(description = "Position", example = "null")
        private String position;

        @JsonProperty("email")
        @Schema(description = "Email", example = "null")
        private String email;

        @JsonProperty("timeZone")
        @Schema(description = "Time zone", example = "null")
        private String timeZone;

        @JsonProperty("language")
        @Schema(description = "Language code", example = "ru")
        private String language;

        @JsonProperty("_instanceName")
        @Schema(description = "Instance name (legacy field)", example = "feruz [feruz]")
        private String instanceName;

        @JsonProperty("locale")
        @Schema(description = "Locale code", example = "ru")
        private String locale;

        // NOTE: "university" field is NOT in old-hemis /app/rest/v2/userInfo response!
        // Removed for 100% backward compatibility with old-hemis
    }
}
