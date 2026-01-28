package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_specialty")
    private UUID specialty;

    @JsonProperty("_faculty")
    private UUID faculty;

    @JsonProperty("_curriculum")
    private UUID curriculum;

    @JsonProperty("academic_year")
    private String academicYear;

    @JsonProperty("course")
    private Integer course;

    @JsonProperty("capacity")
    private Integer capacity;

    @JsonProperty("student_count")
    private Integer studentCount;

    @JsonProperty("_education_type")
    private String educationType;

    @JsonProperty("_education_form")
    private String educationForm;

    @JsonProperty("_education_lang")
    private String educationLang;

    @JsonProperty("active")
    private Boolean active;
}
