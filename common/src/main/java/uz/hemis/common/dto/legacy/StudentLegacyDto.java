package uz.hemis.common.dto.legacy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Student Legacy DTO - OLD-HEMIS CUBA Format Compatible
 *
 * <p><strong>Purpose:</strong> 100% backward compatibility with OLD-HEMIS REST API</p>
 * <p><strong>Format:</strong> CUBA Platform entity serialization format</p>
 *
 * <p>Key differences from modern DTO:</p>
 * <ul>
 *   <li>_entityName field on every object</li>
 *   <li>Nested objects instead of IDs</li>
 *   <li>All reference fields are full objects with their own nested references</li>
 *   <li>Field order matches OLD-HEMIS exactly</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    // OLD-HEMIS exact field order (from actual response)
    "_entityName", "_instanceName", "id", "currentEducationYear", "isGraduate", "country",
    "statusOrderDate", "decreeInfoName", "educationType", "groupId", "language", "socialCategory",
    "educationYear", "educationForm", "faculty", "points", "academicMobileType", "transferCountry",
    "povertyLevel", "studentSuccess", "specialityMaster", "currentTerrain", "statusEducationYear",
    "specialityDoctoral", "createTs", "graduationYear", "tag", "decreeInfoNumber", "responsiblePersonPhone",
    "terrain", "geoAddress", "admissionType", "serialNumber", "specialityOrdinatura", "currentSoato",
    "active", "statusOrderCategory", "decreeInfoDate", "academicReason", "lastname", "groupName",
    "statusOrderNumber", "nationality", "phone", "livingStatus", "roommateType", "grantType", "status",
    "enrollOrderName", "pinfl", "birthday", "studentType", "firstname", "code", "paymentForm", "gender",
    "university", "soato", "parentPhone", "speciality", "enrollOrderDate", "expelReason", "enrollOrderNumber",
    "stipendRate", "course", "studentStatus", "roommateCount", "isDuplicate", "email", "address",
    "citizenship", "eduStartDate", "passportGivenDate", "verified", "currentAddress", "fathername",
    "transferUniversity", "graduationDate", "statusOrderName", "accomodation", "enrollOrderCategory",
    "transferType", "studyDuration", "specialityBachelor", "updateTs", "doctoralStudentType"
})
public class StudentLegacyDto {

    @JsonProperty("_entityName")
    private String entityName = "hemishe_EStudent";

    @JsonProperty("_instanceName")
    private String instanceName;

    private UUID id;
    private String code;
    private String pinfl;
    private String serialNumber;
    private String firstname;
    private String lastname;
    private String fathername;
    private String fullname;
    private LocalDate birthday;
    private String phone;
    private String email;
    private String address;
    private String currentAddress;
    private String parentPhone;
    private String responsiblePersonPhone;
    private String geoAddress;
    private LocalDate passportGivenDate;
    private String tag;
    private String groupId;
    private String groupName;
    private String isGraduate;
    private Boolean isDuplicate;
    private String enrollOrderName;
    private LocalDate enrollOrderDate;
    private String enrollOrderNumber;
    private String enrollOrderCategory;

    // Status Order Fields (OLD-HEMIS backward compatibility)
    private String statusOrderName;
    private LocalDate statusOrderDate;
    private String statusOrderNumber;
    private String statusOrderCategory;

    // Points field for verification system
    private String points;

    private String specialityName;
    private String specialityCode;
    private String commonSpecialityName;
    private String commonSpecialityCode;

    private Integer version;
    private Boolean active;
    private Boolean verified;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createTs;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime updateTs;
    private String createdBy;
    private String updatedBy;

    // Nested objects - CUBA format
    private SimpleReferenceDto country;
    private SimpleReferenceDto educationType;
    private SimpleReferenceDto educationYear;
    private SimpleReferenceDto educationForm;
    private SimpleReferenceDto language;
    private SimpleReferenceDto socialCategory;
    private SimpleReferenceDto studentStatus;
    private SimpleReferenceDto citizenship;
    private SimpleReferenceDto gender;
    private SimpleReferenceDto nationality;
    private SimpleReferenceDto paymentForm;
    private SimpleReferenceDto grantType;
    private SimpleReferenceDto studentType;
    private SimpleReferenceDto course;
    private SimpleReferenceDto accomodation;
    private SimpleReferenceDto livingStatus;
    private SimpleReferenceDto roommateType;
    private SimpleReferenceDto statusEducationYear;
    private SimpleReferenceDto expelReason;
    private SimpleReferenceDto currentEducationYear;
    private SimpleReferenceDto stipendRate;
    private SimpleReferenceDto doctoralStudentType;
    private SimpleReferenceDto admissionType;
    private SimpleReferenceDto transferCountry;
    private String transferUniversity;
    private SimpleReferenceDto transferType;
    private SimpleReferenceDto academicMobileType;
    private SimpleReferenceDto povertyLevel;
    private SimpleReferenceDto academicReason;
    private SimpleReferenceDto graduationYear;
    private SimpleReferenceDto specialityMaster;
    private SimpleReferenceDto specialityDoctoral;
    private SimpleReferenceDto specialityOrdinatura;

    // Complex nested objects
    private UniversityReferenceDto university;
    private FacultyReferenceDto faculty;
    private SoatoReferenceDto soato;
    private SoatoReferenceDto currentSoato;
    private TerrainReferenceDto terrain;
    private TerrainReferenceDto currentTerrain;
    private SpecialityReferenceDto specialityBachelor;

    private Integer roommateCount;

    // Empty list for CUBA compatibility
    private List<Object> studentSuccess = List.of();

    /**
     * Simple reference DTO for classifiers
     * Field order: _entityName, id, nameRu, code, name, active, nameEn, version, nameUz
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"_entityName", "_instanceName", "id", "nameRu", "code", "name", "active", "nameEn", "version", "nameUz"})
    public static class SimpleReferenceDto {
        @JsonProperty("_entityName")
        private String entityName;
        @JsonProperty("_instanceName")
        private String instanceName;
        private String id;
        private String code;
        private String name;
        private String nameRu;
        private String nameEn;
        private String nameUz;
        private Boolean active;
        private Integer version;
    }

    /**
     * University reference with nested properties
     * Field order: _entityName, id, studentUrl, code, universityType, tin, versionType, addStudent,
     *              address, accreditationEdit, active, universityContractCategory, version,
     *              allowGrouping, teacherUrl, allowTransferOutside, ownership, name, gpaEdit
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "_entityName", "_instanceName", "id", "studentUrl", "code", "universityType", "tin", "versionType",
        "addStudent", "address", "accreditationEdit", "active", "universityContractCategory",
        "version", "oneId", "allowGrouping", "teacherUrl", "allowTransferOutside", "ownership", "name", "gpaEdit",
        "addForeignStudent", "gradingSystem", "uzbmbUrl", "universityUrl", "mailAddress", "bankInfo", "accreditationInfo",
        "addAcademicMobileStudent"
    })
    public static class UniversityReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_EUniversity";
        @JsonProperty("_instanceName")
        private String instanceName;
        private String id;
        private String code;
        private String name;
        private String studentUrl;
        private String teacherUrl;
        private String tin;
        private String address;
        private Boolean active;
        private Boolean addStudent;
        private Boolean addTransferStudent;
        private Boolean allowGrouping;
        private Boolean allowTransferOutside;
        private Boolean accreditationEdit;
        private Boolean oneId;
        private Boolean gpaEdit;
        private Integer version;

        // 7 ta yangi maydon (OLD-HEMIS compatibility uchun)
        private Boolean addForeignStudent;
        private Boolean gradingSystem;
        private String uzbmbUrl;
        private String universityUrl;
        private String mailAddress;
        private String bankInfo;
        private String accreditationInfo;

        private Boolean addAcademicMobileStudent;

        private SimpleReferenceDto universityType;
        private SimpleReferenceDto ownership;
        private SimpleReferenceDto versionType;
        private SimpleReferenceDto universityContractCategory;
    }

    /**
     * Faculty/Department reference
     * Field order: _entityName, id, code, version, nameUz, nameRu, nameEn, university
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"_entityName", "_instanceName", "id", "code", "version", "nameUz", "nameRu", "nameEn", "university"})
    public static class FacultyReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_EUniversityDepartment";
        @JsonProperty("_instanceName")
        private String instanceName;
        private String id;
        private String code;
        private String nameUz;
        private String nameRu;
        private String nameEn;
        private Integer version;

        // Nested university reference (OLD-HEMIS compatibility uchun)
        private UniversityReferenceDto university;
    }

    /**
     * SOATO (location classifier) reference
     * Field order: _entityName, id, code, version, name_ru, parent_code, name_uz
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"_entityName", "_instanceName", "id", "code", "version", "name_ru", "parent_code", "name_uz"})
    public static class SoatoReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_HSoato";
        @JsonProperty("_instanceName")
        private String instanceName;
        private String id;
        private String code;
        @JsonProperty("name_uz")
        private String nameUz;
        @JsonProperty("name_ru")
        private String nameRu;
        private Integer version;

        @JsonProperty("parent_code")
        private SoatoReferenceDto parentCode;
    }

    /**
     * Terrain (mahalla) reference
     * Field order: _entityName, id, code, soato, version, nameRu, name
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"_entityName", "_instanceName", "id", "code", "soato", "version", "nameRu", "name"})
    public static class TerrainReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_HTerrain";
        @JsonProperty("_instanceName")
        private String instanceName;
        private String id;
        private String code;
        private String name;
        private String nameRu;
        private Integer version;

        private SoatoReferenceDto soato;
    }

    /**
     * Speciality reference
     * Field order: _entityName, id, code, version, name
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"_entityName", "_instanceName", "id", "code", "version", "name"})
    public static class SpecialityReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_HSpecialityBachelor";
        @JsonProperty("_instanceName")
        private String instanceName;
        private String id;
        private String code;
        private String name;
        private Integer version;
    }
}
