package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.common.enums.RoleType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role summary for user responses")
public class RoleSummary {

    @Schema(description = "Role ID")
    private UUID id;

    @Schema(description = "Role code", example = "UNIVERSITY_ADMIN")
    private String code;

    @Schema(description = "Role name", example = "University Administrator")
    private String name;

    @Schema(description = "Role type", example = "SYSTEM")
    private RoleType roleType;
}
