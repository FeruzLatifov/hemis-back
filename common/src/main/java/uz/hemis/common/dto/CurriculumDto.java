package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Curriculum DTO - API Response/Request for Curriculum
 *
 * <p><strong>CRITICAL - Legacy JSON Field Names:</strong></p>
 * <p>All field names with underscore prefixes MUST be preserved for backward compatibility
 * with 200+ universities using the old CUBA Platform API.</p>
 *
 * <p><strong>JSON Field Mapping (FROZEN - NO CHANGES ALLOWED):</strong></p>
 * <ul>
 *   <li>_university → _university (NOT university)</li>
 *   <li>_specialty → _specialty (NOT specialty)</li>
 *   <li>_education_type → _education_type (NOT educationType)</li>
 *   <li>_education_form → _education_form (NOT educationForm)</li>
 *   <li>_curriculum_type → _curriculum_type (NOT curriculumType)</li>
 * </ul>
 *
 * <p><strong>Audit Fields:</strong> NOT included in DTO (internal only)</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * Curriculum ID (UUID)
     * JSON: "id"
     */
    @JsonProperty("id")
    private UUID id;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Curriculum code
     * JSON: "code"
     */
    @JsonProperty("code")
    private String code;

    /**
     * Curriculum name
     * JSON: "name"
     */
    @JsonProperty("name")
    private String name;

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
     * Specialty ID
     * JSON: "_specialty" (NOT "specialty")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * References: hemishe_e_specialty.id (UUID)
     */
    @JsonProperty("_specialty")
    private UUID specialty;

    // =====================================================
    // Academic Fields
    // =====================================================

    /**
     * Academic year
     * JSON: "academic_year"
     * Format: "2024/2025"
     */
    @JsonProperty("academic_year")
    private String academicYear;

    /**
     * Total credits
     * JSON: "total_credits"
     */
    @JsonProperty("total_credits")
    private Integer totalCredits;

    /**
     * Study duration in years
     * JSON: "study_duration"
     */
    @JsonProperty("study_duration")
    private Integer studyDuration;

    // =====================================================
    // Classifiers (LEGACY FIELD NAMES WITH _)
    // =====================================================

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
     * Curriculum type code
     * JSON: "_curriculum_type" (NOT "curriculumType")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_curriculum_type")
    private String curriculumType;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Active flag
     * JSON: "active"
     */
    @JsonProperty("active")
    private Boolean active;

    /**
     * Approved flag
     * JSON: "is_approved"
     */
    @JsonProperty("is_approved")
    private Boolean isApproved;
}
