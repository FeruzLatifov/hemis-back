package uz.hemis.common.dto.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for AcademicScore entity.
 * Used for API responses and requests.
 *
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicScoreDto {

    private UUID id;

    // University
    private String universityCode;
    private String universityName;

    // Faculty
    private String facultyCode;
    private String facultyName;

    // Education Type
    private String educationTypeCode;
    private String educationTypeName;

    // Education Year
    private String educationYearCode;
    private String educationYearName;

    // Semester
    private String semesterTypeCode;
    private String semesterTypeName;

    // Course
    private String courseCode;
    private String courseName;

    // Score Fields
    private String tableType;
    private Double scorePercent;
    private String scoreType;
    private Double debitorCount;
    private LocalDate updateDate;

    // Audit fields
    private LocalDateTime createTs;
    private String createdBy;
    private LocalDateTime updateTs;
    private String updatedBy;
    private Integer version;
}
