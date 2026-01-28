package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("exam_name")
    private String examName;

    @JsonProperty("_course")
    private UUID course;

    @JsonProperty("_group")
    private UUID group;

    @JsonProperty("_teacher")
    private UUID teacher;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_auditorium")
    private UUID auditorium;

    @JsonProperty("exam_date")
    private LocalDate examDate;

    @JsonProperty("start_time")
    private LocalTime startTime;

    @JsonProperty("end_time")
    private LocalTime endTime;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    @JsonProperty("academic_year")
    private String academicYear;

    @JsonProperty("semester")
    private Integer semester;

    @JsonProperty("_exam_type")
    private String examType;

    @JsonProperty("max_score")
    private Integer maxScore;

    @JsonProperty("passing_score")
    private Integer passingScore;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("is_published")
    private Boolean isPublished;
}
