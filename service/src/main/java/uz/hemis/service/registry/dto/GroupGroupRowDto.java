package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Group Group Row DTO - Root level (University) aggregation for Study Groups registry.
 *
 * <p>Purpose: Display university rows in lazy-loaded tree table with group counts.</p>
 * <p>Frontend: Shows OTM as root rows with study-group counts (read-only registry).</p>
 */
@Schema(
    name = "GroupGroupRow",
    description = "University group row with aggregated study-group statistics (Tree root level)"
)
public record GroupGroupRowDto(

    @Schema(description = "University code (OTM code)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String universityCode,

    @Schema(description = "University name",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String universityName,

    @Schema(description = "Total number of study groups (active + inactive)",
        requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    Long groupCount,

    @Schema(description = "Number of active study groups",
        requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    Long activeGroupCount,

    @Schema(description = "Number of inactive study groups",
        requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    Long inactiveGroupCount,

    @Schema(description = "Flag indicating this row has children (always true for groups)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean hasChildren
) {
    /**
     * Convenience constructor computing inactive count and hasChildren from raw counts.
     */
    public GroupGroupRowDto(String universityCode, String universityName, Long groupCount, Long activeGroupCount) {
        this(
            universityCode,
            universityName,
            groupCount,
            activeGroupCount,
            groupCount - activeGroupCount,
            true
        );
    }
}
