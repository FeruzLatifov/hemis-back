package uz.hemis.common.dto.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Webhook target response DTO (admin UI uchun).
 *
 * <p><strong>Diqqat:</strong> {@code secret_hash} hech qachon API javobida qaytarilmaydi —
 * faqat {@code WebhookSecretResponse} yangi generate paytida plain secret beradi.</p>
 *
 * <p><strong>callbackUrl + active (2026-05-18):</strong> javobda URL derive qilingan
 * holatda kelishi mumkin (admin UI ko'rsata olishi uchun), university'dan derive.</p>
 *
 * @since ADR-0012
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id", "universityCode", "callbackUrl", "description",
        "active", "timeoutMs", "maxRetries",
        "createdAt", "createdBy", "updatedAt", "updatedBy"
})
public record WebhookTargetDto(
        UUID id,
        String universityCode,
        String callbackUrl,    // derived: protocol+university.student_url+suffix (admin UI preview)
        String description,
        Boolean active,        // derived: university.active
        Integer timeoutMs,
        Integer maxRetries,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
