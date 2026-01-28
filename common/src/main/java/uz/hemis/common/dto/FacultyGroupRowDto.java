package uz.hemis.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyGroupRowDto {
    private String universityId;
    private String universityName;
    private Long facultyCount;
    private String statusSummary;
    @Builder.Default
    private Boolean hasChildren = true;
}

