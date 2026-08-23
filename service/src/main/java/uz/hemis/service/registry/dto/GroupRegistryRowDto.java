package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Group Registry Row DTO - Child level (Study group details) for Study Groups registry.
 *
 * <p>Purpose: Display study-group rows when a university is expanded (read-only).</p>
 * <p>Frontend: Shows guruhlar as children of OTM.</p>
 */
@Schema(
    name = "GroupRegistryRow",
    description = "Study group row in tree table (displayed when university is expanded)"
)
public record GroupRegistryRowDto(

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
