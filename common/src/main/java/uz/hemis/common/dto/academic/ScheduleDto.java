package uz.hemis.common.dto.academic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
    "id",
    "_university", "_group", "_course", "_teacher", "_auditorium",
    "schedule_date", "start_time", "end_time",
    "day_of_week", "pair_number",
    "academic_year", "semester", "week_number",
    "_lesson_type", "_schedule_type",
    "active", "is_cancelled"
})
public class ScheduleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_group")
    private UUID group;

    @JsonProperty("_course")
    private UUID course;

    @JsonProperty("_teacher")
    private UUID teacher;

    @JsonProperty("_auditorium")
    private UUID auditorium;

    @JsonProperty("schedule_date")
    private LocalDate scheduleDate;

    @JsonProperty("start_time")
    private LocalTime startTime;

    @JsonProperty("end_time")
    private LocalTime endTime;

    @JsonProperty("day_of_week")
    private Integer dayOfWeek;

    @JsonProperty("pair_number")
    private Integer pairNumber;

    @JsonProperty("academic_year")
    private String academicYear;

    @JsonProperty("semester")
    private Integer semester;

    @JsonProperty("week_number")
    private Integer weekNumber;

    @JsonProperty("_lesson_type")
    private String lessonType;

    @JsonProperty("_schedule_type")
    private String scheduleType;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("is_cancelled")
    private Boolean isCancelled;
}
