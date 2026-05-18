package uz.hemis.common.dto.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Webhook delivery log entry (admin UI history view).
 *
 * @since ADR-0012
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebhookDeliveryLogDto(
        UUID id,
        UUID eventId,
        String eventType,
        String universityCode,
        Integer attemptN,
        Integer httpStatus,
        String responseBody,
        String errorMessage,
        Integer durationMs,
        String status,
        LocalDateTime dispatchedAt,
        LocalDateTime completedAt,
        LocalDateTime nextRetryAt
) {
}
