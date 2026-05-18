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
 * @since ADR-0012
 */
@Schema(description = "Yangi webhook target ro'yxatdan o'tkazish (Univer URL + retry config)")
public record WebhookTargetCreateRequest(

        @NotBlank
        @Pattern(regexp = "\\d{3,10}", message = "Faqat raqamlar (3-10 ta)")
        @Schema(example = "337", description = "OTM identifikator (hemis_NNN dagi NNN)")
        String universityCode,

        @NotBlank
        @Pattern(
                regexp = "^https?://.+",
                message = "URL https:// (yoki http://localhost dev) bilan boshlanishi kerak"
        )
        @Size(max = 500)
        @Schema(
                example = "https://hemis_337.univer.uz/api/hemis-callback/event",
                description = "Univer callback endpoint URL"
        )
        String callbackUrl,

        @Size(max = 255)
        @Schema(example = "Toshkent davlat universiteti", description = "Inson o'qiy oladigan tavsif")
        String description,

        @Min(1000)
        @Max(60000)
        @Schema(example = "30000", defaultValue = "30000", description = "HTTP timeout (ms)")
        Integer timeoutMs,

        @Min(0)
        @Max(10)
        @Schema(example = "5", defaultValue = "5", description = "Maksimal retry urinishlar")
        Integer maxRetries
) {
}
