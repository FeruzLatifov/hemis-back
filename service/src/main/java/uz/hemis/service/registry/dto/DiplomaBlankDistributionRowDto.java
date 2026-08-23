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

    @Schema(description = "Row id (UUID)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "University code")
    String universityCode,

    @Schema(description = "University name")
    String universityName,

    @Schema(description = "Education-year classifier code")
    String educationYear,

    @Schema(description = "Education-year name")
    String educationYearName,

    @Schema(description = "Education-type classifier code")
    String educationType,

    @Schema(description = "Education-type name")
    String educationTypeName,

    @Schema(description = "Blank-category classifier code")
    String blankCategory,

    @Schema(description = "Blank-category name")
    String blankCategoryName,

    @Schema(description = "Blank series")
    String blankSeria,

    @Schema(description = "Range start (inclusive)")
    Integer blankStartNumber,

    @Schema(description = "Range end (inclusive)")
    Integer blankEndNumber,

    @Schema(description = "Quantity (end - start + 1)")
    Integer quantity,

    @Schema(description = "Generate-status classifier code")
    String generateStatusCode,

    @Schema(description = "Generate-status name")
    String generateStatusName,

    @Schema(description = "Distribution date")
    LocalDate distributionDate,

    @Schema(description = "Note")
    String note
) {}
