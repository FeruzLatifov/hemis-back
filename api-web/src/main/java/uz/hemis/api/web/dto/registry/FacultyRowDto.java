package uz.hemis.api.web.dto.registry;

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
@Schema(description = "Faculty Row - Individual faculty details")
public class FacultyRowDto {

    @Schema(description = "Faculty code (Primary Key)", example = "100001-11")
    private String code;

    @Schema(description = "Faculty name (Uzbek)", example = "Iqtisodiyot fakulteti")
    private String nameUz;

    @Schema(description = "Faculty name (Russian)", example = "Экономический факультет")
    private String nameRu;

    @Schema(description = "Parent university code", example = "100001")
    private String universityCode;

    @Schema(description = "Parent university name", example = "O'zbekiston Milliy universiteti")
    private String universityName;

    @Schema(description = "Faculty status (active/inactive)", example = "true")
    private Boolean status;

    @Schema(description = "Parent code (if nested structure)", example = "100001")
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
