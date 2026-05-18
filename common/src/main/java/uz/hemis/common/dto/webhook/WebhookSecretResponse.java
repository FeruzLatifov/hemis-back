package uz.hemis.common.dto.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Yangi yaratilgan webhook secret response.
 *
 * <p><strong>Diqqat — bu plain secret faqat BIR MARTA qaytariladi:</strong></p>
 * <ul>
 *   <li>Markaz secret'ni bcrypt hash sifatida DB'da saqlaydi</li>
 *   <li>OTM IT bu plain qiymatni darhol {@code .env} faylga yozishi kerak</li>
 *   <li>Yo'qotilgan secret restore qilinmaydi — yangi {@code regenerate-secret}</li>
 * </ul>
 *
 * @since ADR-0012
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Yangi yaratilgan HMAC secret. Plain qiymat faqat shu javobda — saqlang.")
public record WebhookSecretResponse(
        UUID targetId,
        String universityCode,
        @Schema(example = "whsec_a7b3c9...", description = "Plain HMAC secret — OTM .env'ga yoziladi")
        String plainSecret,
        LocalDateTime createdAt,
        String warning
) {
    public static WebhookSecretResponse create(UUID id, String code, String secret) {
        return new WebhookSecretResponse(
                id, code, secret, LocalDateTime.now(),
                "Bu plain secret faqat shu javobda qaytariladi. Yo'qotilgan secret tiklash uchun yangi regenerate kerak."
        );
    }
}
