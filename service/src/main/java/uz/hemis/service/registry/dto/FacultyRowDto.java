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
        example = "00001-01",
        required = true
    )
    private String code;

    @Schema(
        description = "Faculty name in Uzbek (Latin script)",
        example = "Axborot texnologiyalari fakulteti",
        required = true
    )
    private String nameUz;

    @Schema(
        description = "Faculty name in Russian (Cyrillic script)",
        example = "Факультет информационных технологий"
    )
    private String nameRu;

    @Schema(
        description = "Parent university code",
        example = "00001",
        required = true
    )
    private String universityCode;

    @Schema(
        description = "Parent university name",
        example = "TATU",
        required = true
    )
    private String universityName;

    @Schema(
        description = "Active status (true=active, false=inactive)",
        example = "true",
        required = true
    )
    private Boolean status;

    @Schema(
        description = "Parent department code (for hierarchical structure)",
        example = "00001"
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
