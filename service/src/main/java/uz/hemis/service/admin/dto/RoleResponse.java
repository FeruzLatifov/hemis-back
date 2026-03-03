package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.hemis.common.enums.RoleType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role response")
public class RoleResponse {

    @Schema(description = "Role ID")
    private UUID id;

    @Schema(description = "Role code")
    private String code;

    @Schema(description = "Role name")
    private String name;

    @Schema(description = "Role description")
    private String description;

    @Schema(description = "Role type (SYSTEM, UNIVERSITY, CUSTOM)")
    private RoleType roleType;

    @Schema(description = "Whether the role is active")
    private Boolean active;

    @Schema(description = "Assigned permissions")
    private List<PermissionResponse> permissions;

    @Schema(description = "Number of users with this role")
    private Integer usersCount;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Permission summary")
    public static class PermissionResponse {
        private UUID id;
        private String code;
        private String name;
        private String category;
        private String resource;
        private String action;
    }
}
