package uz.hemis.common.dto.university;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "id", "code", "name", "shortName",
    "universityCode", "universityName",
    "facultyType", "facultyTypeName",
    "active",
    "createdAt", "createdBy", "updatedAt", "updatedBy"
})
public class FacultyDetailDto {
    private UUID id;
    private String code;
    private String name;
    private String shortName;
    private String universityCode;
    private String universityName;
    private String facultyType;
    private String facultyTypeName;
    private Boolean active;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

