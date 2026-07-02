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
        example = "00001",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityCode;

    @Schema(
        description = "University name (full name in Uzbek/Russian)",
        example = "Toshkent Axborot Texnologiyalari Universiteti",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityName;

    @Schema(
        description = "Total number of departments (active + inactive)",
        example = "12",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long departmentCount;

    @Schema(
        description = "Number of active departments",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long activeDepartmentCount;

    @Schema(
        description = "Number of inactive departments",
        example = "2",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Long inactiveDepartmentCount;

    @Schema(
        description = "Flag indicating this row has children (always true for groups)",
        example = "true",
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
