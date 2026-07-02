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

    @Schema(description = "Group id (UUID primary key)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "OTM-side external group id", example = "12345")
    String groupId,

    @Schema(description = "Group name", example = "715-21 (KI)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String groupName,

    @Schema(description = "Parent university code", example = "00001",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String universityCode,

    @Schema(description = "Parent university name", example = "TATU",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String universityName,

    @Schema(description = "Education type classifier code", example = "11")
    String educationTypeCode,

    @Schema(description = "Education type name (resolved label, falls back to code)", example = "Bakalavr")
    String educationTypeName,

    @Schema(description = "Education year classifier code", example = "2024")
    String educationYearCode,

    @Schema(description = "Education year name (resolved label, falls back to code)", example = "2024-2025")
    String educationYearName,

    @Schema(description = "Active status (true=active, false=inactive)", example = "true")
    Boolean active
) {}
