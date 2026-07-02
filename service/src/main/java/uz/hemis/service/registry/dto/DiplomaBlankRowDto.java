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

    @Schema(description = "Blank id (UUID primary key)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Blank code (serial on printed form)", example = "AB 1234567")
    String blankCode,

    @Schema(description = "Series", example = "AB")
    String series,

    @Schema(description = "Number", example = "1234567")
    String number,

    @Schema(description = "University code", example = "00001")
    String universityCode,

    @Schema(description = "University name", example = "TATU")
    String universityName,

    @Schema(description = "Blank type", example = "BACHELOR")
    String blankType,

    @Schema(description = "Status code", example = "AVAILABLE")
    String statusCode,

    @Schema(description = "Date received by university")
    LocalDate receivedDate,

    @Schema(description = "Date issued/assigned")
    LocalDate issuedDate,

    @Schema(description = "Academic year", example = "2024")
    Integer academicYear,

    @Schema(description = "Active status", example = "true")
    Boolean active
) {}
