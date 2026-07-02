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
