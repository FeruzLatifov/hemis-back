package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Department Detail DTO - Full department information
 *
 * Purpose: Display detailed department information in drawer/modal
 * Frontend: Shown when user clicks on department row
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "DepartmentDetail",
    description = "Complete department information with audit fields (for detail view)"
)
public class DepartmentDetailDto {

    @Schema(
        description = "Department code (Primary key)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;

    @Schema(
        description = "Department name in Uzbek (Latin)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nameUz;

    @Schema(
        description = "Department name in Russian (Cyrillic)"
    )
    private String nameRu;

    @Schema(
        description = "University code (Foreign key)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityCode;

    @Schema(
        description = "University name",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String universityName;

    @Schema(
        description = "Active status",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean status;

    @Schema(
        description = "Department type code"
    )
    private String departmentType;

    @Schema(
        description = "Department type name (localized)"
    )
    private String departmentTypeName;

    @Schema(
        description = "Parent department code (for hierarchical navigation)"
    )
    private String parentCode;

    @Schema(
        description = "Hierarchical path (slash-separated)"
    )
    private String path;

    @Schema(
        description = "Creation timestamp (ISO-8601)",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime createdAt;

    @Schema(
        description = "Username who created this record"
    )
    private String createdBy;

    @Schema(
        description = "Last update timestamp (ISO-8601)",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime updatedAt;

    @Schema(
        description = "Username who last updated this record"
    )
    private String updatedBy;

    @Schema(
        description = "Optimistic locking version"
    )
    private Integer version;
}
