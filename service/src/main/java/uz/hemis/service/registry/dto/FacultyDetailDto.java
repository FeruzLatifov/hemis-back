package uz.hemis.service.registry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Faculty Detail DTO - Full faculty information
 * 
 * Purpose: Display detailed faculty information in drawer/modal
 * Frontend: Shown when user clicks on faculty row
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDetailDto {

    private String code;

    private String nameUz;

    private String nameRu;

    private String universityCode;

    private String universityName;

    private Boolean status;

    private String departmentType;

    private String departmentTypeName;

    private String parentCode;

    private String path;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;

    private Integer version;
}
