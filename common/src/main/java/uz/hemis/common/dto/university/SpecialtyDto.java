package uz.hemis.common.dto.university;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Specialty DTO - API Response/Request for Specialty
 *
 * Maps to hemishe_e_university_speciality table
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
// CUBA klient field tartibi FROZEN
@JsonPropertyOrder({
    "id", "specialityCode", "specialityName",
    "_university", "_faculty", "_education_type", "_education_year",
    "active"
})
public class SpecialtyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("specialityCode")
    private String code;

    @JsonProperty("specialityName")
    private String name;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_faculty")
    private String faculty;

    @JsonProperty("_education_type")
    private String educationType;

    @JsonProperty("_education_year")
    private String educationYear;

    @JsonProperty("active")
    private Boolean active;
}
