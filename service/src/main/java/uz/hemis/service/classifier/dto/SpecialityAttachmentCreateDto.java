package uz.hemis.service.classifier.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

        // Mandatory: the column is NOT NULL with a FK to h_education_form(code). The exact value set is
        // validated in the service against the classifier (clean 400) — no hard-coded @Pattern here.
        @NotBlank(message = "educationForm is required")
        String educationForm,

        /** Academic year of the assignment (2026 = 2026-2027). MANDATORY — the caller must pick a
         *  concrete year (the FE year dropdown is sourced from the classifier's actual years). */
        @NotNull(message = "eduYear is required")
        @Min(value = 1991, message = "eduYear out of range")
        @Max(value = 2100, message = "eduYear out of range")
        Integer eduYear,

        /** Attachment status — ACTIVE (Faol) / SUSPENDED (Nofaol). Optional: defaults to ACTIVE when
         *  omitted (a freshly-attached speciality is active unless the admin marks it otherwise). */
        @Pattern(regexp = "ACTIVE|SUSPENDED|REVOKED", message = "status must be ACTIVE, SUSPENDED or REVOKED")
        String status
) {
}
