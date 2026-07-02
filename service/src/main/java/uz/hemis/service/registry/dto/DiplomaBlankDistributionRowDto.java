package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Diploma Blank Distribution Row DTO — one serial-range allocation with resolved names.
 *
 * <p>Source table: {@code diploma_blank_distribution} (read via native query with
 * classifier LEFT JOINs, raw-code fallback for names).</p>
 */
@Schema(name = "DiplomaBlankDistributionRow", description = "Diploma-blank distribution row (serial-range allocation)")
public record DiplomaBlankDistributionRowDto(

    @Schema(description = "Row id (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "University code", example = "00001")
    String universityCode,

    @Schema(description = "University name", example = "TATU")
    String universityName,

    @Schema(description = "Education-year classifier code", example = "2024")
    String educationYear,

    @Schema(description = "Education-year name", example = "2024-2025")
    String educationYearName,

    @Schema(description = "Education-type classifier code", example = "11")
    String educationType,

    @Schema(description = "Education-type name", example = "Bakalavriat")
    String educationTypeName,

    @Schema(description = "Blank-category classifier code", example = "01")
    String blankCategory,

    @Schema(description = "Blank-category name", example = "Diplom")
    String blankCategoryName,

    @Schema(description = "Blank series", example = "AB")
    String blankSeria,

    @Schema(description = "Range start (inclusive)", example = "1000")
    Integer blankStartNumber,

    @Schema(description = "Range end (inclusive)", example = "1099")
    Integer blankEndNumber,

    @Schema(description = "Quantity (end - start + 1)", example = "100")
    Integer quantity,

    @Schema(description = "Generate-status classifier code", example = "NEW")
    String generateStatusCode,

    @Schema(description = "Generate-status name", example = "Yangi")
    String generateStatusName,

    @Schema(description = "Distribution date")
    LocalDate distributionDate,

    @Schema(description = "Note")
    String note
) {}
