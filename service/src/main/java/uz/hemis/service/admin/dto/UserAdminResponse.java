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

    @Schema(description = "Username (login)")
    private String username;

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Phone")
    private String phone;

    @Schema(description = "PINFL (JSHSHIR, 14 digits). PII — present ONLY when the caller holds "
            + "the 'pinfl.view' permission; null/omitted otherwise.")
    private String pinfl;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Middle name")
    private String middleName;

    @Schema(description = "Passport series+number. PII — present ONLY with 'pinfl.view'.")
    private String passport;

    @Schema(description = "Birth date (yyyy-MM-dd)")
    private java.time.LocalDate birthDate;

    @Schema(description = "Birth place")
    private String birthPlace;

    @Schema(description = "Gender")
    private String gender;

    @Schema(description = "Nationality")
    private String nationality;

    @Schema(description = "Registered address")
    private String address;

    @Schema(description = "University code")
    private String universityCode;

    @Schema(description = "University name")
    private String universityName;

    @Schema(description = "User type")
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
