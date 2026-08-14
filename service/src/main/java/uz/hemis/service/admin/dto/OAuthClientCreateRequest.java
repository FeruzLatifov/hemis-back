package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTM API client (oauth_client) creation request — machine account for the Univer client_credentials API")
public class OAuthClientCreateRequest {

    @NotBlank(message = "client_id is required")
    @Size(max = 100)
    @Schema(description = "Client login / identifier — used as client_id (e.g. otm999)", example = "otm999")
    private String clientId;

    @NotBlank(message = "client_secret is required")
    @Size(min = 4, max = 255)
    @Schema(description = "Client secret (password) — stored BCrypt-hashed, never returned")
    private String clientSecret;

    @NotBlank(message = "universityCode is required")
    @Size(max = 255)
    @Schema(description = "University code this client belongs to", example = "999")
    private String universityCode;

    @Size(max = 255)
    @Schema(description = "Display name (optional; defaults to the university)")
    private String clientName;

    @Schema(description = "Active on creation (default true)", example = "true")
    private Boolean active;
}
