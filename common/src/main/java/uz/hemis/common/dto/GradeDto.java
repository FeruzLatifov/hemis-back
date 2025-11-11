package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Grade DTO - API Response/Request for Grade
 *
 * <p><strong>CRITICAL - Legacy JSON Field Names:</strong></p>
 * <p>All field names with underscore prefixes MUST be preserved for backward compatibility
 * with 200+ universities using the old CUBA Platform API.</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    /**
     * Student ID
     * JSON: "_student" (NOT "student")
     */
    @JsonProperty("_student")
    private UUID student;

    /**
     * Course ID
     * JSON: "_course" (NOT "course")
     */
    @JsonProperty("_course")
    private UUID course;

    /**
     * University code
     * JSON: "_university" (NOT "university")
     */
    @JsonProperty("_university")
    private String university;

    /**
     * Teacher ID
     * JSON: "_teacher" (NOT "teacher")
     */
    @JsonProperty("_teacher")
    private UUID teacher;

    @JsonProperty("grade_value")
    private Integer gradeValue;

    @JsonProperty("grade_letter")
    private String gradeLetter;

    @JsonProperty("grade_points")
    private Double gradePoints;

    @JsonProperty("grade_date")
    private LocalDate gradeDate;

    @JsonProperty("academic_year")
    private String academicYear;

    @JsonProperty("semester")
    private Integer semester;

    @JsonProperty("attempt_number")
    private Integer attemptNumber;

    /**
     * Assessment type code
     * JSON: "_assessment_type" (NOT "assessmentType")
     */
    @JsonProperty("_assessment_type")
    private String assessmentType;

    /**
     * Grade type code
     * JSON: "_grade_type" (NOT "gradeType")
     */
    @JsonProperty("_grade_type")
    private String gradeType;

    @JsonProperty("is_passed")
    private Boolean isPassed;

    @JsonProperty("is_finalized")
    private Boolean isFinalized;
}
