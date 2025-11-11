package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Student DTO for API JSON serialization
 *
 * <p><strong>CRITICAL - API Contract Preservation:</strong></p>
 * <ul>
 *   <li>Field names (@JsonProperty) match legacy JSON EXACTLY</li>
 *   <li>Underscore prefixes preserved (_university, _student_status, etc.)</li>
 *   <li>NO breaking changes to field names, types, or structure</li>
 *   <li>200+ universities depend on this JSON contract</li>
 * </ul>
 *
 * <p><strong>NO-RENAME • NO-DELETE • NO-BREAKING-CHANGES</strong></p>
 *
 * @since 1.0.0
 */
@Data
public class StudentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key & Business Key
    // =====================================================

    /**
     * Student ID (UUID)
     * JSON field: "id"
     */
    @JsonProperty("id")
    private UUID id;

    /**
     * Student code (business identifier)
     * JSON field: "code"
     */
    @JsonProperty("code")
    @NotBlank(message = "Student code is required")
    private String code;

    // =====================================================
    // Personal Information
    // =====================================================

    /**
     * First name
     * JSON field: "firstname"
     */
    @JsonProperty("firstname")
    private String firstname;

    /**
     * Last name
     * JSON field: "lastname"
     */
    @JsonProperty("lastname")
    private String lastname;

    /**
     * Father's name
     * JSON field: "fathername"
     */
    @JsonProperty("fathername")
    private String fathername;

    /**
     * PINFL (14-digit personal ID)
     * JSON field: "pinfl"
     */
    @JsonProperty("pinfl")
    private String pinfl;

    /**
     * Date of birth
     * JSON field: "birthday"
     */
    @JsonProperty("birthday")
    private LocalDate birthday;

    /**
     * Serial number (passport/ID)
     * JSON field: "serial_number"
     */
    @JsonProperty("serial_number")
    private String serialNumber;

    /**
     * Phone number
     * JSON field: "phone"
     */
    @JsonProperty("phone")
    private String phone;

    // =====================================================
    // Location
    // =====================================================

    /**
     * Permanent address
     * JSON field: "address"
     */
    @JsonProperty("address")
    private String address;

    /**
     * Current address
     * JSON field: "current_address"
     */
    @JsonProperty("current_address")
    private String currentAddress;

    /**
     * SOATO code
     * JSON field: "_soato"
     *
     * <p>CRITICAL: Field name starts with underscore - legacy convention</p>
     */
    @JsonProperty("_soato")
    private String soato;

    /**
     * Current SOATO code
     * JSON field: "_current_soato"
     */
    @JsonProperty("_current_soato")
    private String currentSoato;

    // =====================================================
    // University References (with underscore prefix)
    // =====================================================

    /**
     * University code
     * JSON field: "_university"
     *
     * <p>CRITICAL: Underscore prefix preserved from legacy API</p>
     */
    @JsonProperty("_university")
    private String university;

    /**
     * Faculty code
     * JSON field: "_faculty"
     */
    @JsonProperty("_faculty")
    private String faculty;

    /**
     * Speciality code
     * JSON field: "_speciality"
     */
    @JsonProperty("_speciality")
    private String speciality;

    // =====================================================
    // Education Classifiers (all with underscore prefix)
    // =====================================================

    /**
     * Student status code
     * JSON field: "_student_status"
     */
    @JsonProperty("_student_status")
    private String studentStatus;

    /**
     * Payment form code
     * JSON field: "_payment_form"
     */
    @JsonProperty("_payment_form")
    private String paymentForm;

    /**
     * Education type code
     * JSON field: "_education_type"
     */
    @JsonProperty("_education_type")
    private String educationType;

    /**
     * Education form code
     * JSON field: "_education_form"
     */
    @JsonProperty("_education_form")
    private String educationForm;

    /**
     * Course code
     * JSON field: "_course"
     */
    @JsonProperty("_course")
    private String course;

    /**
     * Education year code
     * JSON field: "_education_year"
     */
    @JsonProperty("_education_year")
    private String educationYear;

    /**
     * Gender code
     * JSON field: "_gender"
     */
    @JsonProperty("_gender")
    private String gender;

    /**
     * Nationality code
     * JSON field: "_nationality"
     */
    @JsonProperty("_nationality")
    private String nationality;

    /**
     * Citizenship code
     * JSON field: "_citizenship"
     */
    @JsonProperty("_citizenship")
    private String citizenship;

    /**
     * Country code
     * JSON field: "_country"
     */
    @JsonProperty("_country")
    private String country;

    /**
     * Language code
     * JSON field: "_language"
     */
    @JsonProperty("_language")
    private String language;

    /**
     * Social category code
     * JSON field: "_social_category"
     */
    @JsonProperty("_social_category")
    private String socialCategory;

    // =====================================================
    // Additional Fields
    // =====================================================

    /**
     * Generic status
     * JSON field: "status"
     */
    @JsonProperty("status")
    private String status;

    /**
     * Active flag
     * JSON field: "active"
     */
    @JsonProperty("active")
    private Boolean active;

    /**
     * Verified flag
     * JSON field: "verified"
     */
    @JsonProperty("verified")
    private Boolean verified;

    /**
     * Verification points
     * JSON field: "points"
     */
    @JsonProperty("points")
    private String points;

    // =====================================================
    // NOTE: NO DELETE FIELDS
    // =====================================================
    // deleteTs, deletedBy are internal implementation details
    // NOT exposed in API JSON
    // Soft delete is transparent to external systems
    // =====================================================

    // =====================================================
    // Computed Fields (optional - can be added without breaking contract)
    // =====================================================

    /**
     * Full name (computed field - optional)
     * JSON field: "full_name" (if included)
     *
     * <p>This is an OPTIONAL field - can be added without breaking contract</p>
     */
    @JsonProperty(value = "full_name", access = JsonProperty.Access.READ_ONLY)
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (lastname != null) sb.append(lastname);
        if (firstname != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(firstname);
        }
        if (fathername != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(fathername);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
