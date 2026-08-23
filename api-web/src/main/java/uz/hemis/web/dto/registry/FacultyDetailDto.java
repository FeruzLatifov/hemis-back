package uz.hemis.web.dto.registry;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Faculty Detail - Complete faculty information")
public class FacultyDetailDto {

    @Schema(description = "Faculty code (Primary Key)")
    private String code;

    @Schema(description = "Faculty name (Uzbek)")
    private String nameUz;

    @Schema(description = "Faculty name (Russian)")
    private String nameRu;

    @Schema(description = "Parent university code")
    private String universityCode;

    @Schema(description = "Parent university name")
    private String universityName;

    @Schema(description = "Faculty status (active/inactive)")
    private Boolean status;

    @Schema(description = "Department type code")
    private String departmentType;

    @Schema(description = "Department type name")
    private String departmentTypeName;

    @Schema(description = "Parent code (if nested)")
    private String parentCode;

    @Schema(description = "Hierarchical path")
    private String path;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;

    @Schema(description = "Updated by")
    private String updatedBy;

    @Schema(description = "Version (optimistic locking)")
    private Integer version;
}
