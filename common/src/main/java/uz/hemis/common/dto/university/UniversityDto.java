package uz.hemis.common.dto.university;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
// Field tartibi 200+ universitet CUBA klient (Univer Yii2 PHP) bilan FROZEN — declaration order
@JsonPropertyOrder({
    "code",
    "tin", "name", "address", "cadastre",
    "university_url", "student_url", "teacher_url", "uzbmb_url",
    "_soato", "_soato_region",
    "_university_type", "_ownership", "_university_version",
    "_university_activity_status", "_university_belongs_to",
    "_university_contract_category", "_parent_university",
    "active", "gpa_edit", "accreditation_edit", "add_student",
    "allow_grouping", "allow_transfer_outside",
    "_version_type", "_terrain",
    "mail_address", "bank_info", "accreditation_info",
    "add_foreign_student", "grading_system", "one_id",
    "add_transfer_student", "add_academic_mobile_student",
    "allow_academic_import", "is_financial_independent",
    "_ownership_name", "_university_type_name",
    "_university_activity_status_name", "_university_belongs_to_name",
    "_university_contract_category_name", "_university_version_name",
    "_soato_name", "_soato_region_name", "_terrain_name"
})
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

    /**
     * Add foreign student allowed flag
     * JSON: "add_foreign_student"
     */
    @JsonProperty("add_foreign_student")
    private Boolean addForeignStudent;

    /**
     * Grading system flag
     * JSON: "grading_system"
     */
    @JsonProperty("grading_system")
    private Boolean gradingSystem;

    /**
     * OneID integration flag
     * JSON: "one_id"
     */
    @JsonProperty("one_id")
    private Boolean oneId;

    /**
     * Add transfer student flag
     * JSON: "add_transfer_student"
     */
    @JsonProperty("add_transfer_student")
    private Boolean addTransferStudent;

    /**
     * Add academic mobile student flag
     * JSON: "add_academic_mobile_student"
     */
    @JsonProperty("add_academic_mobile_student")
    private Boolean addAcademicMobileStudent;

    /**
     * Allow academic import flag — Univer ArchiveController gates archive import on this.
     * JSON: "allow_academic_import"
     */
    @JsonProperty("allow_academic_import")
    private Boolean allowAcademicImport;

    /**
     * Financial independence flag.
     * JSON: "is_financial_independent"
     */
    @JsonProperty("is_financial_independent")
    private Boolean isFinancialIndependent;

    // =====================================================
    // Resolved display names (NOT persisted, populated via ClassifierLookupService)
    // Populated by service layer for read responses; ignored on writes.
    // =====================================================

    @JsonProperty("_ownership_name")
    private String ownershipName;

    @JsonProperty("_university_type_name")
    private String universityTypeName;

    @JsonProperty("_university_activity_status_name")
    private String universityActivityStatusName;

    @JsonProperty("_university_belongs_to_name")
    private String universityBelongsToName;

    @JsonProperty("_university_contract_category_name")
    private String universityContractCategoryName;

    @JsonProperty("_university_version_name")
    private String universityVersionName;

    /** Region name resolved from `_soato` (4-digit SOATO). */
    @JsonProperty("_soato_name")
    private String soatoName;

    /** District name resolved from `_soato_region` (7-digit SOATO). */
    @JsonProperty("_soato_region_name")
    private String soatoRegionName;

    /** Neighborhood/mahalla name resolved from `_terrain`. */
    @JsonProperty("_terrain_name")
    private String terrainName;
}
