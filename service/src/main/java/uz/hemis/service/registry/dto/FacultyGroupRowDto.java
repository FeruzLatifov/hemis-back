package uz.hemis.service.registry.dto;

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
public class FacultyGroupRowDto {

    private String universityCode;

    private String universityName;

    private Long facultyCount;

    private Long activeFacultyCount;

    private Long inactiveFacultyCount;

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
