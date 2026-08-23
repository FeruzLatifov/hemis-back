package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Department Group Row DTO - Root level (University) aggregation
 *
 * Purpose: Display university rows in lazy-loaded tree table
 * Frontend: Shows OTM as root rows with department count
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "DepartmentGroupRow",
    description = "University group row with aggregated department statistics (Tree root level)"
)
public class DepartmentGroupRowDto {

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
        description = "Total number of departments (active + inactive)",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long departmentCount;

    @Schema(
        description = "Number of active departments",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long activeDepartmentCount;

    @Schema(
        description = "Number of inactive departments",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long inactiveDepartmentCount;

    @Schema(
        description = "Flag indicating this row has children (always true for groups)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean hasChildren;

    public DepartmentGroupRowDto(String universityCode, String universityName, Long departmentCount, Long activeDepartmentCount) {
        this.universityCode = universityCode;
        this.universityName = universityName;
        this.departmentCount = departmentCount;
        this.activeDepartmentCount = activeDepartmentCount;
        this.inactiveDepartmentCount = departmentCount - activeDepartmentCount;
        this.hasChildren = true;
    }
}
