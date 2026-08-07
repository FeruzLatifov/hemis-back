package uz.hemis.service.classifier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

        @Size(max = 32)
        String educationForm
) {
}
