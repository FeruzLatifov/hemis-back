package uz.hemis.common.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Webhook target o'zgartirish (partial update).
 *
 * <p>Faqat berilgan maydonlar yangilanadi (null = saqlanadi).</p>
 *
 * <p><strong>callbackUrl + active bu yerda yo'q (2026-05-18):</strong>
 * URL {@code hemishe_e_university.student_url}'dan keladi (admin u yerda yangilaydi),
 * active flag university'dan keladi.</p>
 *
 * @since ADR-0012
 */
@Schema(description = "Webhook target — description/timeout/retries yangilash")
public record WebhookTargetUpdateRequest(

        @Size(max = 255)
        String description,

        @Min(1000) @Max(60000)
        Integer timeoutMs,

        @Min(0) @Max(10)
        Integer maxRetries
) {
}
