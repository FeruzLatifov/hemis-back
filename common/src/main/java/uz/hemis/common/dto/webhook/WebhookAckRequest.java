package uz.hemis.common.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Univer → markaz apply ack body (K2). HMAC-imzolangan POST'ning JSON tanasi.
 *
 * <p>Univer {@code ApplyHemisEventJob} event'ni apply qilgach (yoki xato bo'lgach)
 * markazga shu body bilan ack yuboradi → {@code webhook_apply_result}.</p>
 *
 * @since ADR-0012 (K2)
 */
public record WebhookAckRequest(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("status") String status,          // 'applied' | 'failed'
        @JsonProperty("error_message") String errorMessage
) {
}
