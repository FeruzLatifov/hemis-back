package uz.hemis.common.dto.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Univer-side apply natijasi (K2, admin UI view).
 *
 * <p>"Delivered != applied" — markaz adminkada qaysi OTM da apply muvaffaqiyatsiz bo'lganini
 * va sababini shu DTO orqali ko'radi.</p>
 *
 * @since ADR-0012 (K2)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebhookApplyResultDto(
        UUID eventId,
        String universityCode,
        String status,
        LocalDateTime appliedAt,
        String errorMessage,
        LocalDateTime reportedAt
) {
}
