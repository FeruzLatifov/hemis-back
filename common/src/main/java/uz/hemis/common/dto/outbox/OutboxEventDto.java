package uz.hemis.common.dto.outbox;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbox event admin DTO — admin UI inspect/retry/replay uchun.
 *
 * <p>Payload — short preview (first 500 chars). To'liq payload alohida endpoint
 * orqali (DB load qisqartirish — list endpoint'da hech kim full JSONB ko'rmaydi).</p>
 *
 * @since 2026-05-19 (ADR-0007 Stage 1, admin observability)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OutboxEventDto(
        UUID id,
        String aggregateType,
        String aggregateId,
        String eventType,
        Integer schemaVersion,
        String topic,
        String payloadPreview,     // first N chars (admin list)
        LocalDateTime occurredAt,
        LocalDateTime publishedAt, // null = pending
        Integer retryCount,
        String lastError,
        String correlationId,
        String causationId,
        String createdBy,
        String status              // PENDING | PUBLISHED | DLQ | RETRYING
) {
}
