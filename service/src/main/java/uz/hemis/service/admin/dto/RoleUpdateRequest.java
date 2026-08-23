package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role update request")
public class RoleUpdateRequest {

    @Size(max = 255, message = "Name must be at most 255 characters")
    @Schema(description = "Role name (human-readable)")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @Schema(description = "Role description", nullable = true)
    private String description;

    @Schema(description = "Permission IDs to assign (replaces existing)")
    private Set<UUID> permissionIds;
}
