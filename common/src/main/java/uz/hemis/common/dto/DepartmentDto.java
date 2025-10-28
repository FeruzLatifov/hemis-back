package uz.hemis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Department (Cathedra) DTO
 *
 * Legacy JSON field names preserved with @JsonProperty
 * for backward compatibility with 200+ universities
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto implements Serializable {

    private UUID id;

    @JsonProperty("department_code")
    private String departmentCode;

    @JsonProperty("department_name")
    private String departmentName;

    @JsonProperty("department_name_uz")
    private String departmentNameUz;

    @JsonProperty("department_name_ru")
    private String departmentNameRu;

    @JsonProperty("department_name_en")
    private String departmentNameEn;

    @JsonProperty("_university")
    private String university;

    @JsonProperty("_faculty")
    private UUID faculty;

    @JsonProperty("_head")
    private UUID head;

    @JsonProperty("_department_type")
    private String departmentType;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("room_number")
    private String roomNumber;

    @JsonProperty("building")
    private String building;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("description")
    private String description;

    @JsonProperty("sort_order")
    private Integer sortOrder;

    // Audit fields
    @JsonProperty("create_ts")
    private LocalDateTime createTs;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("update_ts")
    private LocalDateTime updateTs;

    @JsonProperty("updated_by")
    private String updatedBy;
}
