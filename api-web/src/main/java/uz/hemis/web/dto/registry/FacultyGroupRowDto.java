package uz.hemis.web.dto.registry;

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

    @Schema(description = "University code (Primary Key)")
    private String universityCode;

    @Schema(description = "University name")
    private String universityName;

    @Schema(description = "Total faculty count in this university")
    private Long facultyCount;

    @Schema(description = "Active faculty count")
    private Long activeFacultyCount;

    @Schema(description = "Inactive faculty count")
    private Long inactiveFacultyCount;

    @Schema(description = "Has children (always true for groups)")
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
