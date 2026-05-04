package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for DoctoralStudent entity
 *
 * Table: hemishe_e_doctorate_student
 * All fields match actual entity from ministry.sql
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// FROZEN: api-legacy /entities/hemishe_EDoctorateStudent/* CUBA klient
@JsonPropertyOrder({
    "id", "u_id",
    "first_name", "second_name", "third_name",
    "passport_number", "passport_pin", "birth_date",
    "dissertation_theme", "home_address",
    "accepted_date", "student_id_number",
    "_science_branch", "_payment_form",
    "_citizenship", "_nationality", "_gender", "_country",
    "_province", "_district", "_soato",
    "_doctoral_student_type", "_doctorate_student_status",
    "_level", "_university", "_department", "_position",
    "active", "_translations",
    "_speciality", "_education_year",
    "create_ts", "created_by", "update_ts", "updated_by"
})
public class DoctoralStudentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    @JsonProperty("u_id")
    private Integer uId;

    // Personal Information
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("second_name")
    private String secondName;

    @JsonProperty("third_name")
    private String thirdName;

    @JsonProperty("passport_number")
    private String passportNumber;

    @JsonProperty("passport_pin")
    private String passportPin;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    // Academic Information
    @JsonProperty("dissertation_theme")
    private String dissertationTheme;

    @JsonProperty("home_address")
    private String homeAddress;

    @JsonProperty("accepted_date")
    private LocalDate acceptedDate;

    @JsonProperty("student_id_number")
    private String studentIdNumber;

    // Classifier References
    @JsonProperty("_science_branch")
    private String scienceBranch;

    @JsonProperty("_payment_form")
    private String paymentForm;

    @JsonProperty("_citizenship")
    private String citizenship;

    @JsonProperty("_nationality")
    private String nationality;

    @JsonProperty("_gender")
    private String gender;

    @JsonProperty("_country")
    private String country;

    @JsonProperty("_province")
    private String province;

    @JsonProperty("_district")
    private String district;

    @JsonProperty("_soato")
    private String soato;

    @JsonProperty("_doctoral_student_type")
    private String doctoralStudentType;

    @JsonProperty("_doctorate_student_status")
    private String doctorateStudentStatus;

    @JsonProperty("_level")
    private String level;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_department")
    private String department;

    @JsonProperty("_position")
    private Integer position;

    private Boolean active;

    @JsonProperty("_translations")
    private String translations;

    @JsonProperty("_speciality")
    private UUID speciality;

    @JsonProperty("_education_year")
    private String educationYear;

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
