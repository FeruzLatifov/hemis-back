package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Diploma Blank Distribution create/update request DTO.
 *
 * <p>Bean Validation: {@code universityCode} and {@code blankSeria} required;
 * {@code blankStartNumber}/{@code blankEndNumber} required. The
 * {@code end >= start} rule is enforced in the service layer.</p>
 */
@Schema(name = "DiplomaBlankDistributionRequest", description = "Create/update payload for a diploma-blank distribution")
public record DiplomaBlankDistributionRequestDto(

    @Schema(description = "University code", example = "00001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "universityCode is required")
    @Size(max = 255)
    String universityCode,

    @Schema(description = "Education-year classifier code", example = "2024")
    @Size(max = 32)
    String educationYear,

    @Schema(description = "Education-type classifier code", example = "11")
    @Size(max = 32)
    String educationType,

    @Schema(description = "Blank-category classifier code", example = "01")
    @Size(max = 32)
    String blankCategory,

    @Schema(description = "Blank series", example = "AB", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "blankSeria is required")
    @Size(max = 32)
    String blankSeria,

    @Schema(description = "Range start (inclusive)", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "blankStartNumber is required")
    Integer blankStartNumber,

    @Schema(description = "Range end (inclusive, >= start)", example = "1099", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "blankEndNumber is required")
    Integer blankEndNumber,

    @Schema(description = "Generate-status classifier code", example = "NEW")
    @Size(max = 32)
    String generateStatusCode,

    @Schema(description = "Distribution date")
    LocalDate distributionDate,

    @Schema(description = "Note")
    String note
) {}
