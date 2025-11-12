package uz.hemis.api.web.dto.registry;

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

    @Schema(description = "Faculty code (Primary Key)", example = "100001-11")
    private String code;

    @Schema(description = "Faculty name (Uzbek)", example = "Iqtisodiyot fakulteti")
    private String nameUz;

    @Schema(description = "Faculty name (Russian)", example = "Экономический факультет")
    private String nameRu;

    @Schema(description = "Parent university code", example = "100001")
    private String universityCode;

    @Schema(description = "Parent university name", example = "O'zbekiston Milliy universiteti")
    private String universityName;

    @Schema(description = "Faculty status (active/inactive)", example = "true")
    private Boolean status;

    @Schema(description = "Department type code", example = "11")
    private String departmentType;

    @Schema(description = "Department type name", example = "Fakultet")
    private String departmentTypeName;

    @Schema(description = "Parent code (if nested)", example = "100001")
    private String parentCode;

    @Schema(description = "Hierarchical path", example = "100001/100001-11")
    private String path;

    @Schema(description = "Created at", example = "2020-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Created by", example = "admin")
    private String createdBy;

    @Schema(description = "Updated at", example = "2023-05-20T14:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Updated by", example = "admin")
    private String updatedBy;

    @Schema(description = "Version (optimistic locking)", example = "1")
    private Integer version;
}
