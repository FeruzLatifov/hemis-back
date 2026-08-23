package uz.hemis.api.legacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
// CUBA klient response wrapper old-hemis bilan bir xil tartibda bo'lishi shart
@JsonPropertyOrder({"success", "status", "statusText", "responseTime", "data"})
@Schema(description = "Legacy user info response wrapper (old-hemis format)")
public class LegacyUserInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("success")
    @Schema(description = "Success flag")
    @Builder.Default
    private Boolean success = true;

    @JsonProperty("status")
    @Schema(description = "HTTP status code")
    @Builder.Default
    private Integer status = 200;

    @JsonProperty("statusText")
    @Schema(description = "Status text")
    @Builder.Default
    private String statusText = "";

    @JsonProperty("responseTime")
    @Schema(description = "Response time in milliseconds")
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
    // CUBA convention: old-hemis User payload field tartibi
    @JsonPropertyOrder({
            "id", "login", "name", "firstName", "middleName", "lastName",
            "position", "email", "timeZone", "language",
            "_instanceName", "locale", "university"
    })
    @Schema(description = "User data")
    public static class UserData implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("id")
        @Schema(description = "User ID (UUID)")
        private String id;

        @JsonProperty("login")
        @Schema(description = "User login")
        private String login;

        @JsonProperty("name")
        @Schema(description = "Full name")
        private String name;

        @JsonProperty("firstName")
        @Schema(description = "First name")
        private String firstName;

        @JsonProperty("middleName")
        @Schema(description = "Middle name")
        private String middleName;

        @JsonProperty("lastName")
        @Schema(description = "Last name")
        private String lastName;

        @JsonProperty("position")
        @Schema(description = "Position")
        private String position;

        @JsonProperty("email")
        @Schema(description = "Email")
        private String email;

        @JsonProperty("timeZone")
        @Schema(description = "Time zone")
        private String timeZone;

        @JsonProperty("language")
        @Schema(description = "Language code")
        private String language;

        @JsonProperty("_instanceName")
        @Schema(description = "Instance name (legacy field)")
        private String instanceName;

        @JsonProperty("locale")
        @Schema(description = "Locale code")
        private String locale;

        @JsonProperty("university")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "University code")
        private String university;
    }
}
