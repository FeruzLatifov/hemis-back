package uz.hemis.service.classifier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Edit an existing speciality→OTM attachment. Only the MUTABLE fields:
 * <ul>
 *   <li>{@code specialityId} — may change, but within the SAME education type + university as the
 *       existing row (re-assigning the university / education type = a new attachment, not an edit);</li>
 *   <li>{@code educationForm} — 11=Kunduzgi / 12=Kechki / 16=Masofaviy;</li>
 *   <li>{@code eduYear} — academic year (2026 = 2026-2027);</li>
 *   <li>{@code status} — ACTIVE (Faol) / SUSPENDED (Nofaol) / REVOKED.</li>
 * </ul>
 * The university and education type are NOT part of this DTO — they are fixed on an existing row.
 */
public record SpecialityAttachmentUpdateDto(
        @Schema(description = "Mutaxassislik UUID (mavjud qatorning ta'lim turi bilan bir xil turdan)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "specialityId is required")
        UUID specialityId,

        @Schema(description = "Ta'lim shakli kodi — h_education_form klassifikatoridan (11=Kunduzgi, 12=Kechki, 13=Sirtqi, 16=Masofaviy, ...)")
        String educationForm,

        @Schema(description = "O'quv yili (2026 = 2026-2027)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "eduYear is required")
        @Min(value = 1991, message = "eduYear out of range")
        @Max(value = 2100, message = "eduYear out of range")
        Integer eduYear,

        @Schema(description = "Holat: ACTIVE (Faol) / SUSPENDED (Nofaol) / REVOKED",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "status is required")
        @Pattern(regexp = "ACTIVE|SUSPENDED|REVOKED", message = "status must be ACTIVE, SUSPENDED or REVOKED")
        String status
) {
}
