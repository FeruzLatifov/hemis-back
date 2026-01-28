package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * University DTO - API Response/Request for University
 *
 * <p><strong>CRITICAL - Legacy JSON Field Names:</strong></p>
 * <p>All field names with underscore prefixes MUST be preserved for backward compatibility
 * with 200+ universities using the old CUBA Platform API.</p>
 *
 * <p><strong>JSON Field Mapping (FROZEN - NO CHANGES ALLOWED):</strong></p>
 * <ul>
 *   <li>code → code (PK)</li>
 *   <li>_soato → _soato (NOT soato)</li>
 *   <li>_soato_region → _soato_region (NOT soatoRegion)</li>
 *   <li>_university_type → _university_type (NOT universityType)</li>
 *   <li>_ownership → _ownership (NOT ownership)</li>
 *   <li>_parent_university → _parent_university (NOT parentUniversity)</li>
 * </ul>
 *
 * <p><strong>Audit Fields:</strong> NOT included in DTO (internal only)</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    /**
     * University code (Primary Key - VARCHAR)
     * JSON: "code"
     */
    @JsonProperty("code")
    private String code;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * TIN - Tax Identification Number
     * JSON: "tin"
     */
    @JsonProperty("tin")
    private String tin;

    /**
     * University name
     * JSON: "name"
     */
    @JsonProperty("name")
    private String name;

    /**
     * Address
     * JSON: "address"
     */
    @JsonProperty("address")
    private String address;

    /**
     * Cadastre information
     * JSON: "cadastre"
     */
    @JsonProperty("cadastre")
    private String cadastre;

    /**
     * University URL
     * JSON: "university_url"
     */
    @JsonProperty("university_url")
    private String universityUrl;

    /**
     * Student portal URL
     * JSON: "student_url"
     */
    @JsonProperty("student_url")
    private String studentUrl;

    /**
     * Teacher portal URL
     * JSON: "teacher_url"
     */
    @JsonProperty("teacher_url")
    private String teacherUrl;

    /**
     * UZBMB URL
     * JSON: "uzbmb_url"
     */
    @JsonProperty("uzbmb_url")
    private String uzbmbUrl;

    // =====================================================
    // Location Classifiers (LEGACY FIELD NAMES WITH _)
    // =====================================================

    /**
     * SOATO code (location)
     * JSON: "_soato" (NOT "soato")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_soato")
    private String soato;

    /**
     * SOATO region code
     * JSON: "_soato_region" (NOT "soatoRegion")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_soato_region")
    private String soatoRegion;

    // =====================================================
    // University Classifiers (LEGACY FIELD NAMES WITH _)
    // =====================================================

    /**
     * University type code
     * JSON: "_university_type" (NOT "universityType")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_university_type")
    private String universityType;

    /**
     * Ownership type code
     * JSON: "_ownership" (NOT "ownership")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_ownership")
    private String ownership;

    /**
     * University version code
     * JSON: "_university_version" (NOT "universityVersion")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_university_version")
    private String universityVersion;

    /**
     * University activity status code
     * JSON: "_university_activity_status" (NOT "universityActivityStatus")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_university_activity_status")
    private String universityActivityStatus;

    /**
     * University belongs to code
     * JSON: "_university_belongs_to" (NOT "universityBelongsTo")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_university_belongs_to")
    private String universityBelongsTo;

    /**
     * University contract category code
     * JSON: "_university_contract_category" (NOT "universityContractCategory")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_university_contract_category")
    private String universityContractCategory;

    /**
     * Parent university code (self-reference)
     * JSON: "_parent_university" (NOT "parentUniversity")
     *
     * CRITICAL: Underscore prefix MUST be preserved!
     */
    @JsonProperty("_parent_university")
    private String parentUniversity;

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
     * GPA edit allowed flag
     * JSON: "gpa_edit"
     */
    @JsonProperty("gpa_edit")
    private Boolean gpaEdit;

    /**
     * Accreditation edit allowed flag
     * JSON: "accreditation_edit"
     */
    @JsonProperty("accreditation_edit")
    private Boolean accreditationEdit;

    /**
     * Add student allowed flag
     * JSON: "add_student"
     */
    @JsonProperty("add_student")
    private Boolean addStudent;

    /**
     * Allow grouping flag
     * JSON: "allow_grouping"
     */
    @JsonProperty("allow_grouping")
    private Boolean allowGrouping;

    /**
     * Allow transfer outside flag
     * JSON: "allow_transfer_outside"
     */
    @JsonProperty("allow_transfer_outside")
    private Boolean allowTransferOutside;

    /**
     * Version type code
     * JSON: "_version_type"
     */
    @JsonProperty("_version_type")
    private String versionType;

    /**
     * Terrain code (mahalla)
     * JSON: "_terrain"
     */
    @JsonProperty("_terrain")
    private String terrain;

    /**
     * Mail address
     * JSON: "mail_address"
     */
    @JsonProperty("mail_address")
    private String mailAddress;

    /**
     * Bank information
     * JSON: "bank_info"
     */
    @JsonProperty("bank_info")
    private String bankInfo;

    /**
     * Accreditation information
     * JSON: "accreditation_info"
     */
    @JsonProperty("accreditation_info")
    private String accreditationInfo;
}
