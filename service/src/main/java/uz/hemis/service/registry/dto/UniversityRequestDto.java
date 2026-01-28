package uz.hemis.service.registry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * University Create/Update Request DTO
 */
@Data
public class UniversityRequestDto {
    
    @NotBlank(message = "Code is required")
    @Size(max = 255)
    private String code;
    
    @Size(max = 255)
    private String tin;
    
    @NotBlank(message = "Name is required")
    @Size(max = 1024)
    private String name;
    
    private String ownership;           // _ownership
    private String soato;               // _soato
    private String soatoRegion;         // _soato_region
    private String universityType;      // _university_type
    private String universityVersion;   // _university_version
    private String activityStatus;      // _university_activity_status
    private String belongsTo;           // _university_belongs_to
    private String contractCategory;    // _university_contract_category
    private String parentUniversity;    // _parent_university
    private String terrain;             // _terrain
    private String versionType;         // _version_type
    
    private String address;
    private String cadastre;
    private String universityUrl;
    private String studentUrl;
    private String teacherUrl;
    private String uzbmbUrl;
    private String mailAddress;
    private String accreditationInfo;
    private String bankInfo;
    
    private Boolean active;
    private Boolean gpaEdit;
    private Boolean accreditationEdit;
    private Boolean addStudent;
    private Boolean allowGrouping;
    private Boolean allowTransferOutside;
}
