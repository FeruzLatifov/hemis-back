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

    @Schema(description = "Blank id (UUID)",
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
    Boolean active,

    @Schema(description = "Supplier/manufacturer")
    String supplier,

    @Schema(description = "Production batch number")
    String batchNumber,

    @Schema(description = "Reason for status change")
    String statusReason
) {}
