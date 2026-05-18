package uz.hemis.common.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Webhook target o'zgartirish (partial update).
 *
 * <p>Faqat berilgan maydonlar yangilanadi (null = saqlanadi).</p>
 *
 * @since ADR-0012
 */
@Schema(description = "Webhook target maydonlarini yangilash (partial)")
public record WebhookTargetUpdateRequest(

        @Pattern(regexp = "^https?://.+")
        @Size(max = 500)
        @Schema(example = "https://hemis_337.univer.uz/api/hemis-callback/event")
        String callbackUrl,

        @Size(max = 255)
        String description,

        @Schema(description = "Vaqtinchalik o'chirish (event yubormaslik)")
        Boolean active,

        @Min(1000) @Max(60000)
        Integer timeoutMs,

        @Min(0) @Max(10)
        Integer maxRetries
) {
}
