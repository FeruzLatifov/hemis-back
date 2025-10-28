package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("_student")
    private UUID student;

    @JsonProperty("_course")
    private UUID course;

    @JsonProperty("_schedule")
    private UUID schedule;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_group")
    private UUID group;

    @JsonProperty("attendance_date")
    private LocalDate attendanceDate;

    @JsonProperty("_attendance_type")
    private String attendanceType;

    @JsonProperty("academic_year")
    private String academicYear;

    @JsonProperty("semester")
    private Integer semester;

    @JsonProperty("week_number")
    private Integer weekNumber;

    @JsonProperty("is_present")
    private Boolean isPresent;

    @JsonProperty("reason")
    private String reason;
}
