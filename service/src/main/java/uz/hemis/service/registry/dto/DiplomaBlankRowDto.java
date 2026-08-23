package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Diploma Blank Registry Row DTO — flat list row for the Diploma-blanks registry (READ-ONLY).
 *
 * <p>Source table: {@code hemishe_e_diploma_blank} (read via EntityManager native query).</p>
 */
@Schema(name = "DiplomaBlankRegistryRow", description = "Diploma-blank row in the flat paginated registry list")
public record DiplomaBlankRowDto(

    @Schema(description = "Blank id (UUID primary key)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Blank code (serial on printed form)")
    String blankCode,

    @Schema(description = "Series")
    String series,

    @Schema(description = "Number")
    String number,

    @Schema(description = "University code")
    String universityCode,

    @Schema(description = "University name")
    String universityName,

    @Schema(description = "Blank type")
    String blankType,

    @Schema(description = "Status code")
    String statusCode,

    @Schema(description = "Date received by university")
    LocalDate receivedDate,

    @Schema(description = "Date issued/assigned")
    LocalDate issuedDate,

    @Schema(description = "Academic year")
    Integer academicYear,

    @Schema(description = "Active status")
    Boolean active
) {}
