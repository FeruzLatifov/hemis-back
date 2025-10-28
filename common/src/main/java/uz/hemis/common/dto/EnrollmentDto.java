package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Enrollment DTO - API Response/Request for Enrollment
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
public class EnrollmentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("enrollment_number")
    private String enrollmentNumber;

    /**
     * Student ID
     * JSON: "_student" (NOT "student")
     */
    @JsonProperty("_student")
    private UUID student;

    /**
     * University code
     * JSON: "_university" (NOT "university")
     */
    @JsonProperty("_university")
    private String university;

    /**
     * Specialty ID
     * JSON: "_specialty" (NOT "specialty")
     */
    @JsonProperty("_specialty")
    private UUID specialty;

    /**
     * Faculty ID
     * JSON: "_faculty" (NOT "faculty")
     */
    @JsonProperty("_faculty")
    private UUID faculty;

    @JsonProperty("enrollment_date")
    private LocalDate enrollmentDate;

    @JsonProperty("academic_year")
    private String academicYear;

    @JsonProperty("course")
    private Integer course;

    /**
     * Education type code
     * JSON: "_education_type" (NOT "educationType")
     */
    @JsonProperty("_education_type")
    private String educationType;

    /**
     * Education form code
     * JSON: "_education_form" (NOT "educationForm")
     */
    @JsonProperty("_education_form")
    private String educationForm;

    /**
     * Payment form code
     * JSON: "_payment_form" (NOT "paymentForm")
     */
    @JsonProperty("_payment_form")
    private String paymentForm;

    /**
     * Enrollment status code
     * JSON: "_enrollment_status" (NOT "enrollmentStatus")
     */
    @JsonProperty("_enrollment_status")
    private String enrollmentStatus;

    @JsonProperty("active")
    private Boolean active;
}
