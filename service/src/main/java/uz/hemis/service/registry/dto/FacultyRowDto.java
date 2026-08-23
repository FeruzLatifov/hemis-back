package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Faculty Row DTO - Child level (Faculty details)
 * 
 * Purpose: Display faculty rows when university is expanded
 * Frontend: Shows fakultetlar as children of OTM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "FacultyRow",
    description = "Faculty row in tree table (displayed when university is expanded)"
)
public class FacultyRowDto {

    @Schema(
        description = "Faculty code (Primary key, unique identifier)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;

    @Schema(
        description = "Faculty name in Uzbek (Latin script)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nameUz;

    @Schema(
        description = "Faculty name in Russian (Cyrillic script)"
    )
    private String nameRu;

    @Schema(
        description = "Parent university code",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityCode;

    @Schema(
        description = "Parent university name",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityName;

    @Schema(
        description = "Active status (true=active, false=inactive)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean status;

    @Schema(
        description = "Parent department code (for hierarchical structure)"
    )
    private String parentCode;

    public FacultyRowDto(String code, String nameUz, String nameRu, String universityCode, String universityName, Boolean status) {
        this.code = code;
        this.nameUz = nameUz;
        this.nameRu = nameRu;
        this.universityCode = universityCode;
        this.universityName = universityName;
        this.status = status;
    }
}
