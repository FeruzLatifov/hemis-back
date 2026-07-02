package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Diploma Blank Registry Detail DTO — single blank detail (READ-ONLY).
 *
 * <p>Row fields + supplier / batch number / status reason.</p>
 */
@Schema(name = "DiplomaBlankRegistryDetail", description = "Diploma-blank detail")
public record DiplomaBlankDetailDto(

    @Schema(description = "Blank id (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
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
    Boolean active,

    @Schema(description = "Supplier/manufacturer", example = "Davlat Belgisi")
    String supplier,

    @Schema(description = "Production batch number", example = "B-2024-001")
    String batchNumber,

    @Schema(description = "Reason for status change", example = "Damaged during transport")
    String statusReason
) {}
