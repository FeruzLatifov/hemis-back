package uz.hemis.common.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Yangi webhook target qo'shish so'rovi.
 *
 * <p><strong>URL convention (2026-05-18):</strong> callbackUrl bu yerda yo'q —
 * URL avtomatik derive bo'ladi: {@code ${protocol}://{university.student_url}${suffix}}.
 * Faqat {@code universityCode} + secret avtomatik generate.</p>
 *
 * @since ADR-0012
 */
@Schema(description = "Yangi webhook target — universityCode + secret avtomatik generate (URL university.student_url'dan)")
public record WebhookTargetCreateRequest(

        @NotBlank
        @Pattern(regexp = "\\d{3,10}", message = "Faqat raqamlar (3-10 ta)")
        @Schema(description = "OTM identifikator (hemishe_e_university.code)")
        String universityCode,

        @Size(max = 255)
        @Schema(description = "Inson o'qiy oladigan tavsif")
        String description,

        @Min(1000)
        @Max(60000)
        @Schema(defaultValue = "30000", description = "HTTP timeout (ms)")
        Integer timeoutMs,

        @Min(0)
        @Max(10)
        @Schema(defaultValue = "3", description = "Maksimal retry urinishlar")
        Integer maxRetries
) {
}
