package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Specialty DTO - API Response/Request for Specialty
 *
 * <p><strong>CRITICAL - Legacy JSON Field Names:</strong></p>
 * <p>All field names with underscore prefixes MUST be preserved for backward compatibility
 * with 200+ universities using the old CUBA Platform API.</p>
 *
 * <p><strong>JSON Field Mapping (FROZEN - NO CHANGES ALLOWED):</strong></p>
 * <ul>
 *   <li>_university → _university (NOT university)</li>
 *   <li>_faculty → _faculty (NOT faculty)</li>
 *   <li>_specialty_type → _specialty_type (NOT specialtyType)</li>
 *   <li>_education_type → _education_type (NOT educationType)</li>
 *   <li>_education_form → _education_form (NOT educationForm)</li>
 *   <li>_study_period → _study_period (NOT studyPeriod)</li>
 * </ul>
 *
 * <p><strong>Audit Fields:</strong> NOT included in DTO (internal only)</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * Specialty ID (UUID)
     * JSON: "id"
     */
    @JsonProperty("id")
    private UUID id;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Specialty code
     * JSON: "code"
     */
    @JsonProperty("code")
    private String code;

    /**
     * Specialty name
     * JSON: "name"
     */
    @JsonProperty("name")
    private String name;

    /**
     * Short name
     * JSON: "short_name"
     */
    @JsonProperty("short_name")
    private String shortName;

    // =====================================================
    // References (LEGACY FIELD NAMES WITH _)
    // =====================================================

    /**
     * University code
     * JSON: "_university" (NOT "university")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * References: hemishe_e_university.code (VARCHAR PK)
     */
    @JsonProperty("_university")
    private String university;

    /**
     * Faculty ID
     * JSON: "_faculty" (NOT "faculty")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * References: hemishe_e_faculty.id (UUID)
     */
    @JsonProperty("_faculty")
    private UUID faculty;

    // =====================================================
    // Classifiers (LEGACY FIELD NAMES WITH _)
    // =====================================================

    /**
     * Specialty type code
     * JSON: "_specialty_type" (NOT "specialtyType")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_specialty_type")
    private String specialtyType;

    /**
     * Education type code
     * JSON: "_education_type" (NOT "educationType")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * Examples: '11' = Bachelor, '12' = Master, '13' = PhD
     */
    @JsonProperty("_education_type")
    private String educationType;

    /**
     * Education form code
     * JSON: "_education_form" (NOT "educationForm")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * Examples: '11' = Full-time, '12' = Part-time, '13' = Evening, '14' = Distance
     */
    @JsonProperty("_education_form")
    private String educationForm;

    /**
     * Study period code
     * JSON: "_study_period" (NOT "studyPeriod")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_study_period")
    private String studyPeriod;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Active flag
     * JSON: "active"
     */
    @JsonProperty("active")
    private Boolean active;
}
