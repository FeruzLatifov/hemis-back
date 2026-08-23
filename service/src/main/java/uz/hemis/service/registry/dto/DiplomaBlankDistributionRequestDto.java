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

    @Schema(description = "University code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "universityCode is required")
    @Size(max = 255)
    String universityCode,

    @Schema(description = "Education-year classifier code")
    @Size(max = 32)
    String educationYear,

    @Schema(description = "Education-type classifier code")
    @Size(max = 32)
    String educationType,

    @Schema(description = "Blank-category classifier code")
    @Size(max = 32)
    String blankCategory,

    @Schema(description = "Blank series", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "blankSeria is required")
    @Size(max = 32)
    String blankSeria,

    @Schema(description = "Range start (inclusive)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "blankStartNumber is required")
    Integer blankStartNumber,

    @Schema(description = "Range end (inclusive, >= start)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "blankEndNumber is required")
    Integer blankEndNumber,

    @Schema(description = "Generate-status classifier code")
    @Size(max = 32)
    String generateStatusCode,

    @Schema(description = "Distribution date")
    LocalDate distributionDate,

    @Schema(description = "Note")
    String note
) {}
