package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Group Detail DTO - Full study-group information for the detail view.
 *
 * <p>Purpose: Display detailed study-group information in drawer/modal (read-only).</p>
 * <p>NOTE: The {@code hemishe_e_university_group} table has NO audit columns,
 * so this DTO intentionally carries NO created/updated/version fields.</p>
 */
@Schema(
    name = "GroupDetail",
    description = "Complete study-group information (no audit fields — source table has none)"
)
public record GroupDetailDto(

    @Schema(description = "Group id (UUID primary key)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "OTM-side external group id")
    String groupId,

    @Schema(description = "Group name",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String groupName,

    @Schema(description = "Parent university code",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String universityCode,

    @Schema(description = "Parent university name",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String universityName,

    @Schema(description = "Education type classifier code")
    String educationTypeCode,

    @Schema(description = "Education type name (resolved label, falls back to code)")
    String educationTypeName,

    @Schema(description = "Education year classifier code")
    String educationYearCode,

    @Schema(description = "Education year name (resolved label, falls back to code)")
    String educationYearName,

    @Schema(description = "Active status (true=active, false=inactive)")
    Boolean active
) {}
