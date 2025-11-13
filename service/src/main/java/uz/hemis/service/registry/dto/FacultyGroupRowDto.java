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
        example = "00001",
        required = true
    )
    private String universityCode;

    @Schema(
        description = "University name (full name in Uzbek/Russian)",
        example = "Toshkent Axborot Texnologiyalari Universiteti",
        required = true
    )
    private String universityName;

    @Schema(
        description = "Total number of faculties (active + inactive)",
        example = "12",
        required = true,
        minimum = "0"
    )
    private Long facultyCount;

    @Schema(
        description = "Number of active faculties",
        example = "10",
        required = true,
        minimum = "0"
    )
    private Long activeFacultyCount;

    @Schema(
        description = "Number of inactive faculties",
        example = "2",
        required = true,
        minimum = "0"
    )
    private Long inactiveFacultyCount;

    @Schema(
        description = "Flag indicating this row has children (always true for groups)",
        example = "true",
        required = true
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
