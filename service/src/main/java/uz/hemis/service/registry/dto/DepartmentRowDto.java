package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Department Row DTO - Child level (Department details)
 *
 * Purpose: Display department rows when university is expanded
 * Frontend: Shows kafedralar as children of OTM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "DepartmentRow",
    description = "Department row in tree table (displayed when university is expanded)"
)
public class DepartmentRowDto {

    @Schema(
        description = "Department code (Primary key, unique identifier)",
        example = "00001-01",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;

    @Schema(
        description = "Department name in Uzbek (Latin script)",
        example = "Axborot texnologiyalari kafedrasi",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nameUz;

    @Schema(
        description = "Department name in Russian (Cyrillic script)",
        example = "Кафедра информационных технологий"
    )
    private String nameRu;

    @Schema(
        description = "Parent university code",
        example = "00001",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityCode;

    @Schema(
        description = "Parent university name",
        example = "TATU",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityName;

    @Schema(
        description = "Active status (true=active, false=inactive)",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean status;

    @Schema(
        description = "Parent department code (for hierarchical structure)",
        example = "00001"
    )
    private String parentCode;

    public DepartmentRowDto(String code, String nameUz, String nameRu, String universityCode, String universityName, Boolean status) {
        this.code = code;
        this.nameUz = nameUz;
        this.nameRu = nameRu;
        this.universityCode = universityCode;
        this.universityName = universityName;
        this.status = status;
    }
}
