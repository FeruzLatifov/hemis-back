package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Teacher DTO - API Response/Request for Teacher
 *
 * <p><strong>CRITICAL - Legacy JSON Field Names:</strong></p>
 * <p>All field names with underscore prefixes MUST be preserved for backward compatibility
 * with 200+ universities using the old CUBA Platform API.</p>
 *
 * <p><strong>JSON Field Mapping (FROZEN - NO CHANGES ALLOWED):</strong></p>
 * <ul>
 *   <li>_university → _university (NOT university)</li>
 *   <li>_gender → _gender (NOT gender)</li>
 *   <li>_academic_degree → _academic_degree (NOT academicDegree)</li>
 *   <li>_academic_rank → _academic_rank (NOT academicRank)</li>
 * </ul>
 *
 * <p><strong>Audit Fields:</strong> NOT included in DTO (internal only)</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * Teacher ID (UUID)
     * JSON: "id"
     */
    @JsonProperty("id")
    private UUID id;

    // =====================================================
    // Personal Information
    // =====================================================

    /**
     * First name
     * JSON: "firstname"
     */
    @JsonProperty("firstname")
    private String firstname;

    /**
     * Last name
     * JSON: "lastname"
     */
    @JsonProperty("lastname")
    private String lastname;

    /**
     * Father's name (patronymic)
     * JSON: "fathername"
     */
    @JsonProperty("fathername")
    private String fathername;

    /**
     * Full name (computed field - backward compatible)
     * JSON: "fullName"
     *
     * This is a computed field for convenience.
     * Old CUBA Platform API may have included this.
     */
    @JsonProperty("fullName")
    private String fullName;

    /**
     * Date of birth
     * JSON: "birthday"
     */
    @JsonProperty("birthday")
    private LocalDate birthday;

    /**
     * Gender code
     * JSON: "_gender" (NOT "gender")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_gender")
    private String gender;

    /**
     * PINFL (Personal Identification Number)
     * JSON: "pinfl"
     * 14 digits unique identifier
     */
    @JsonProperty("pinfl")
    private String pinfl;

    // =====================================================
    // University Reference (LEGACY FIELD NAME WITH _)
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

    // =====================================================
    // Academic Qualifications (LEGACY FIELD NAMES WITH _)
    // =====================================================

    /**
     * Academic degree code
     * JSON: "_academic_degree" (NOT "academicDegree")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * Examples: '11' = candidate, '12' = doctor, '13' = DSc, etc.
     */
    @JsonProperty("_academic_degree")
    private String academicDegree;

    /**
     * Academic rank code
     * JSON: "_academic_rank" (NOT "academicRank")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     * Examples: '11' = assistant, '12' = senior lecturer, '13' = docent, '14' = professor
     */
    @JsonProperty("_academic_rank")
    private String academicRank;
}
