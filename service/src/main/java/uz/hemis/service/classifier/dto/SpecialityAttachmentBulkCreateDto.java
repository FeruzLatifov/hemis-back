package uz.hemis.service.classifier.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Bulk-create payload: attach ONE speciality to ONE OTM in SEVERAL education forms at once.
 *
 * <p>Mirrors {@link SpecialityAttachmentCreateDto} but with a list of {@code educationForms} instead
 * of a single form — so the admin picks the OTM / speciality / year / status once and ticks every
 * form that applies (Kunduzgi + Kechki + Masofaviy …). One live row is created per form; forms that
 * are already attached (for the same speciality + year) are skipped, never a 409 for the whole batch.</p>
 *
 * <p>{@code universityCode} is validated against the caller's server-derived
 * {@link uz.hemis.common.auth.AccessScope} (fail-closed) in the service layer.</p>
 *
 * @since 2.2.0
 */
public record SpecialityAttachmentBulkCreateDto(

        @NotBlank(message = "universityCode is required")
        @Size(max = 255)
        String universityCode,

        @NotNull(message = "specialityId is required")
        UUID specialityId,

        // Each value is a h_education_form(code); the exact set is validated in the service against
        // the classifier (clean 400) — no hard-coded @Pattern here.
        @NotEmpty(message = "at least one educationForm is required")
        List<@NotBlank String> educationForms,

        /** Academic year of the assignment (2026 = 2026-2027) — applied to every created row. */
        @NotNull(message = "eduYear is required")
        @Min(value = 1991, message = "eduYear out of range")
        @Max(value = 2100, message = "eduYear out of range")
        Integer eduYear,

        /** Attachment status — ACTIVE (Faol) / SUSPENDED (Nofaol). Optional: defaults to ACTIVE. */
        @Pattern(regexp = "ACTIVE|SUSPENDED|REVOKED", message = "status must be ACTIVE, SUSPENDED or REVOKED")
        String status
) {
}
