package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Faculty Group Row DTO - Root level (University) aggregation
 * 
 * Purpose: Display university rows in lazy-loaded tree table
 * Frontend: Shows OTM as root rows with faculty count
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "FacultyGroupRow",
    description = "University group row with aggregated faculty statistics (Tree root level)"
)
public class FacultyGroupRowDto {

    @Schema(
        description = "University code (Primary key)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityCode;

    @Schema(
        description = "University name (full name in Uzbek/Russian)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityName;

    @Schema(
        description = "Total number of faculties (active + inactive)",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long facultyCount;

    @Schema(
        description = "Number of active faculties",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long activeFacultyCount;

    @Schema(
        description = "Number of inactive faculties",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long inactiveFacultyCount;

    @Schema(
        description = "Flag indicating this row has children (always true for groups)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean hasChildren;

    public FacultyGroupRowDto(String universityCode, String universityName, Long facultyCount, Long activeFacultyCount) {
        this.universityCode = universityCode;
        this.universityName = universityName;
        this.facultyCount = facultyCount;
        this.activeFacultyCount = activeFacultyCount;
        this.inactiveFacultyCount = facultyCount - activeFacultyCount;
        this.hasChildren = true;
    }
}
