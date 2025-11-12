package uz.hemis.api.web.dto.registry;

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
@Schema(description = "Faculty Group Row - University level aggregation")
public class FacultyGroupRowDto {

    @Schema(description = "University code (Primary Key)", example = "100001")
    private String universityCode;

    @Schema(description = "University name", example = "O'zbekiston Milliy universiteti")
    private String universityName;

    @Schema(description = "Total faculty count in this university", example = "12")
    private Long facultyCount;

    @Schema(description = "Active faculty count", example = "10")
    private Long activeFacultyCount;

    @Schema(description = "Inactive faculty count", example = "2")
    private Long inactiveFacultyCount;

    @Schema(description = "Has children (always true for groups)", example = "true")
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
