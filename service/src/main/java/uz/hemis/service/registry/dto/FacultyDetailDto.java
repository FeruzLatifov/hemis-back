package uz.hemis.service.registry.dto;

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
@Schema(
    name = "FacultyDetail",
    description = "Complete faculty information with audit fields (for detail view)"
)
public class FacultyDetailDto {

    @Schema(
        description = "Faculty code (Primary key)",
        example = "00001-01",
        required = true
    )
    private String code;

    @Schema(
        description = "Faculty name in Uzbek (Latin)",
        example = "Axborot texnologiyalari fakulteti",
        required = true
    )
    private String nameUz;

    @Schema(
        description = "Faculty name in Russian (Cyrillic)",
        example = "Факультет информационных технологий"
    )
    private String nameRu;

    @Schema(
        description = "University code (Foreign key)",
        example = "00001",
        required = true
    )
    private String universityCode;

    @Schema(
        description = "University name",
        example = "Toshkent Axborot Texnologiyalari Universiteti",
        required = true
    )
    private String universityName;

    @Schema(
        description = "Active status",
        example = "true",
        required = true
    )
    private Boolean status;

    @Schema(
        description = "Department type code",
        example = "11"
    )
    private String departmentType;

    @Schema(
        description = "Department type name (localized)",
        example = "Fakultet"
    )
    private String departmentTypeName;

    @Schema(
        description = "Parent department code (for hierarchical navigation)",
        example = "00001"
    )
    private String parentCode;

    @Schema(
        description = "Hierarchical path (slash-separated)",
        example = "00001/00001-01"
    )
    private String path;

    @Schema(
        description = "Creation timestamp (ISO-8601)",
        example = "2023-09-01T10:00:00",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime createdAt;

    @Schema(
        description = "Username who created this record",
        example = "admin"
    )
    private String createdBy;

    @Schema(
        description = "Last update timestamp (ISO-8601)",
        example = "2024-01-15T14:30:00",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime updatedAt;

    @Schema(
        description = "Username who last updated this record",
        example = "rector"
    )
    private String updatedBy;

    @Schema(
        description = "Optimistic locking version",
        example = "3"
    )
    private Integer version;
}
