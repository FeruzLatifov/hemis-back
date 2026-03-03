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
@Schema(description = "User admin response")
public class UserAdminResponse {

    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "Username (login)", example = "john_doe")
    private String username;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Email", example = "john@example.com")
    private String email;

    @Schema(description = "Phone", example = "+998901234567")
    private String phone;

    @Schema(description = "University entity code", example = "TATU")
    private String entityCode;

    @Schema(description = "University name", example = "Toshkent axborot texnologiyalari universiteti")
    private String universityName;

    @Schema(description = "User type", example = "UNIVERSITY")
    private String userType;

    @Schema(description = "Whether user is enabled")
    private Boolean enabled;

    @Schema(description = "Whether account is not locked")
    private Boolean accountNonLocked;

    @Schema(description = "Assigned roles")
    private List<RoleSummary> roles;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
