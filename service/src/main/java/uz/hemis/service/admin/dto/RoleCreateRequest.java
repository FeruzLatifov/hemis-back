package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Schema(description = "Role creation request")
public class RoleCreateRequest {

    @NotBlank(message = "Code is required")
    @Size(min = 2, max = 100, message = "Code must be 2-100 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code must contain only uppercase letters, digits, and underscores")
    @Schema(description = "Role code (machine-readable, unique)", example = "FACULTY_ADMIN")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    @Schema(description = "Role name (human-readable)", example = "Faculty Administrator")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    @Schema(description = "Role description", nullable = true)
    private String description;

    @Schema(description = "Permission IDs to assign")
    private Set<UUID> permissionIds;
}
