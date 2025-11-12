package uz.hemis.service.registry.dto;

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
public class FacultyRowDto {

    private String code;

    private String nameUz;

    private String nameRu;

    private String universityCode;

    private String universityName;

    private Boolean status;

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
