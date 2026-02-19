package uz.hemis.common.dto.student;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lightweight DTO for student list/search results.
 *
 * <p>Contains only the 18 fields needed for the Students table in hemis-front,
 * instead of the full 50+ fields in {@link StudentDto}.</p>
 *
 * <p><strong>Why a separate DTO?</strong></p>
 * <ul>
 *   <li>{@link StudentDto} has 50+ fields — too heavy for list views</li>
 *   <li>Native SQL selects only these 18 columns — ~4x less data from DB</li>
 *   <li>Network payload: ~80KB/page vs ~500KB/page</li>
 *   <li>{@link StudentDto} is NOT modified (legacy API backward compatibility)</li>
 * </ul>
 *
 * <p><strong>JSON field names</strong> match {@link StudentDto} exactly
 * (underscore prefix convention preserved).</p>
 *
 * @since 2.1.0
 * @see StudentDto
 */
public record StudentListDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("code") String code,
        @JsonProperty("firstname") String firstname,
        @JsonProperty("lastname") String lastname,
        @JsonProperty("fathername") String fathername,
        @JsonProperty("pinfl") String pinfl,
        @JsonProperty("_university") String university,
        @JsonProperty("_faculty") String faculty,
        @JsonProperty("_speciality") String speciality,
        @JsonProperty("_student_status") String studentStatus,
        @JsonProperty("_payment_form") String paymentForm,
        @JsonProperty("_education_type") String educationType,
        @JsonProperty("_education_form") String educationForm,
        @JsonProperty("_course") String course,
        @JsonProperty("_education_year") String educationYear,
        @JsonProperty("_gender") String gender,
        @JsonProperty("groupName") String groupName,
        @JsonProperty("active") Boolean active
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Full name (computed) — matches {@link StudentDto#getFullname()} format.
     */
    @JsonProperty(value = "fullname", access = JsonProperty.Access.READ_ONLY)
    public String getFullname() {
        return Stream.of(lastname, firstname, fathername)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }
}
