package uz.hemis.common.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * </ul>
 *
 * @since 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentLegacyDto {

    @JsonProperty("_entityName")
    private String entityName = "hemishe_EStudent";

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
    private String specialityName;
    private String specialityCode;
    private String commonSpecialityName;
    private String commonSpecialityCode;

    private Integer version;
    private Boolean active;
    private Boolean verified;

    private LocalDateTime createTs;
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

    // Complex nested objects
    private UniversityReferenceDto university;
    private FacultyReferenceDto faculty;
    private SoatoReferenceDto soato;
    private SoatoReferenceDto currentSoato;
    private TerrainReferenceDto terrain;
    private TerrainReferenceDto currentTerrain;
    private SpecialityReferenceDto specialityBachelor;

    // Empty list for CUBA compatibility
    private List<Object> studentSuccess = List.of();

    /**
     * Simple reference DTO for classifiers
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SimpleReferenceDto {
        @JsonProperty("_entityName")
        private String entityName;
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
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UniversityReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_EUniversity";
        private String id;
        private String code;
        private String name;
        private String studentUrl;
        private String teacherUrl;
        private String tin;
        private String address;
        private Boolean active;
        private Boolean addStudent;
        private Boolean allowGrouping;
        private Boolean allowTransferOutside;
        private Boolean accreditationEdit;
        private Boolean gpaEdit;
        private Integer version;

        private SimpleReferenceDto universityType;
        private SimpleReferenceDto ownership;
        private SimpleReferenceDto versionType;
        private SimpleReferenceDto universityContractCategory;
    }

    /**
     * Faculty/Department reference
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FacultyReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_EUniversityDepartment";
        private String id;
        private String code;
        private String nameUz;
        private String nameRu;
        private String nameEn;
        private Integer version;
    }

    /**
     * SOATO (location classifier) reference
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SoatoReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_HSoato";
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
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TerrainReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_HTerrain";
        private String id;
        private String code;
        private String name;
        private String nameRu;
        private Integer version;

        private SoatoReferenceDto soato;
    }

    /**
     * Speciality reference
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SpecialityReferenceDto {
        @JsonProperty("_entityName")
        private String entityName = "hemishe_HSpecialityBachelor";
        private String id;
        private String code;
        private String name;
        private Integer version;
    }
}
