package uz.hemis.service.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Webhook envelope — Univer'ga jo'natiladigan JSON payload.
 *
 * <p>Bu Univer side qabul qiladigan format. Field tartibi {@code @JsonPropertyOrder}
 * bilan kafolatlangan (canonical JSON — signature mosligi uchun).</p>
 *
 * <p><strong>Misol payload:</strong></p>
 * <pre>
 * {
 *   "event_id":        "550e8400-e29b-41d4-a716-446655440000",
 *   "event_type":      "classifier.updated",
 *   "aggregate_type":  "classifier",
 *   "aggregate_id":    "123",
 *   "occurred_at":     "2026-05-13T10:30:00",
 *   "schema_version":  1,
 *   "data":            { ... domain-specific payload ... }
 * }
 * </pre>
 *
 * @since ADR-0012
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "event_id",
        "event_type",
        "aggregate_type",
        "aggregate_id",
        "occurred_at",
        "schema_version",
        "data"
})
public record WebhookEventEnvelope(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType,
        @JsonProperty("aggregate_type") String aggregateType,
        @JsonProperty("aggregate_id") String aggregateId,
        @JsonProperty("occurred_at") LocalDateTime occurredAt,
        @JsonProperty("schema_version") Integer schemaVersion,
        @JsonProperty("data") Object data
) {
}
