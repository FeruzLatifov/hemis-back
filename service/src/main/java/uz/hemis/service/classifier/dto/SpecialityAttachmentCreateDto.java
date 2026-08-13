package uz.hemis.service.classifier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Create payload for attaching a unified-classifier speciality to an OTM.
 *
 * <p>The {@code universityCode} is validated against the caller's server-derived
 * {@link uz.hemis.common.auth.AccessScope} (fail-closed) in the service layer — an
 * OTM-tier caller may only attach to its own OTM; a ministry caller to any.</p>
 *
 * @since 2.1.0
 */
public record SpecialityAttachmentCreateDto(

        @NotBlank(message = "universityCode is required")
        @Size(max = 255)
        String universityCode,

        @NotNull(message = "specialityId is required")
        UUID specialityId,

        // Mandatory + constrained: the column is NOT NULL with CHECK (education_form IN '11','12','16').
        // Validating here returns a clean 400 instead of a DB integrity error.
        @NotBlank(message = "educationForm is required")
        @Pattern(regexp = "11|12|16", message = "educationForm must be 11 (Kunduzgi), 12 (Kechki) or 16 (Masofaviy)")
        String educationForm,

        /** Academic year of the assignment (2026 = 2026-2027). Defaults to the current intake year if omitted. */
        Integer eduYear
) {
}
