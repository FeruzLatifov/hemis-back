package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Scholarship DTO
 *
 * Table: hemishe_e_student_scholarship_full
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    @JsonProperty("_student")
    private UUID student;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_education_type")
    private String educationType;

    @JsonProperty("_education_form")
    private String educationForm;

    @JsonProperty("_payment_form")
    private String paymentForm;

    @JsonProperty("_semester")
    private String semester;

    @JsonProperty("_education_year")
    private String educationYear;

    @JsonProperty("_stipend_category")
    private String stipendCategory;

    @JsonProperty("_stipend_type")
    private String stipendType;

    @JsonProperty("decree")
    private String decree;

    @JsonProperty("group")
    private String group;

    @JsonProperty("curriculum")
    private String curriculum;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("local_id")
    private String localId;

    @JsonProperty("semester_number")
    private String semesterNumber;

    // Audit fields
    @JsonProperty("create_ts")
    private LocalDateTime createTs;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("update_ts")
    private LocalDateTime updateTs;

    @JsonProperty("updated_by")
    private String updatedBy;
}
