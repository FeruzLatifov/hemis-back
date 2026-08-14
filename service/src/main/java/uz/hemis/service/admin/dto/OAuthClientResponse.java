package uz.hemis.service.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "OTM API client (oauth_client) — the client_secret is NEVER included")
public class OAuthClientResponse {

    private UUID id;
    private String clientId;
    private String clientName;
    private String clientType;
    private String universityCode;
    private String universityName;
    private Boolean active;
    private List<String> grantTypes;
    private List<String> scopes;
    /** Role codes bound to the client (e.g. OTM_API). */
    private List<String> roles;
    private Integer secretVersion;
    private LocalDateTime secretRotatedAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
