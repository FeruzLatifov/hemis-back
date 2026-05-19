package uz.hemis.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.outbox.OutboxEventDto;
import uz.hemis.common.dto.outbox.OutboxStatsDto;
import uz.hemis.domain.entity.outbox.OutboxEvent;
import uz.hemis.domain.repository.outbox.OutboxEventAdminRepository;
import uz.hemis.domain.repository.outbox.OutboxEventRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Admin observability + manual operations for outbox queue.
 *
 * <ul>
 *   <li>Paginated list with status filter (PENDING / PUBLISHED / DLQ)</li>
 *   <li>Health stats (counts + oldest pending age)</li>
 *   <li>Retry: clears {@code retry_count} so poller picks it up again</li>
 *   <li>Discard: marks {@code published_at = now} without sending (DLQ resolution)</li>
 *   <li>Full payload fetch for inspect drawer (separate from list)</li>
 * </ul>
 *
 * @since 2026-05-19
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OutboxAdminService {

    private static final int PAYLOAD_PREVIEW_LIMIT = 500;
    private static final int DLQ_THRESHOLD = 5;

    private final OutboxEventRepository repository;
    private final OutboxEventAdminRepository adminRepository;

    public Page<OutboxEventDto> list(String status, String aggregateType, Pageable pageable) {
        Page<OutboxEvent> page = adminRepository.search(
                normaliseStatus(status), aggregateType, DLQ_THRESHOLD, pageable);
        return page.map(this::toDto);
    }

    public OutboxEventDto findById(UUID id, boolean includeFullPayload) {
        OutboxEvent event = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + id));
        OutboxEventDto dto = toDto(event);
        if (!includeFullPayload) {
            return dto;
        }
        return new OutboxEventDto(
                dto.id(), dto.aggregateType(), dto.aggregateId(), dto.eventType(),
                dto.schemaVersion(), dto.topic(), event.getPayload(),
                dto.occurredAt(), dto.publishedAt(), dto.retryCount(),
                dto.lastError(), dto.correlationId(), dto.causationId(),
                dto.createdBy(), dto.status()
        );
    }

    public OutboxStatsDto stats() {
        return adminRepository.statsSnapshot(DLQ_THRESHOLD);
    }

    @Transactional
    public OutboxEventDto retry(UUID id) {
        OutboxEvent event = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + id));
        if (event.getPublishedAt() != null) {
            throw new IllegalStateException("Already published, cannot retry: " + id);
        }
        event.setRetryCount(0);
        event.setLastError(null);
        log.warn("Outbox retry triggered manually: id={} aggregateType={}", id, event.getAggregateType());
        return toDto(event);
    }

    @Transactional
    public OutboxEventDto discard(UUID id, String reason) {
        OutboxEvent event = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + id));
        if (event.getPublishedAt() != null) {
            throw new IllegalStateException("Already published, cannot discard: " + id);
        }
        event.setPublishedAt(LocalDateTime.now());
        event.setLastError("DISCARDED_BY_ADMIN: " + (reason == null ? "no reason" : reason));
        log.warn("Outbox discarded manually: id={} reason={}", id, reason);
        return toDto(event);
    }

    private OutboxEventDto toDto(OutboxEvent e) {
        String preview = e.getPayload() == null
                ? null
                : (e.getPayload().length() > PAYLOAD_PREVIEW_LIMIT
                        ? e.getPayload().substring(0, PAYLOAD_PREVIEW_LIMIT) + "…"
                        : e.getPayload());
        return new OutboxEventDto(
                e.getId(),
                e.getAggregateType(),
                e.getAggregateId(),
                e.getEventType(),
                e.getSchemaVersion(),
                e.getKafkaTopic(),
                preview,
                e.getOccurredAt(),
                e.getPublishedAt(),
                e.getRetryCount(),
                e.getLastError(),
                e.getCorrelationId(),
                e.getCausationId(),
                e.getCreatedBy(),
                computeStatus(e)
        );
    }

    private String computeStatus(OutboxEvent e) {
        if (e.getPublishedAt() != null) {
            return "PUBLISHED";
        }
        if (e.getRetryCount() != null && e.getRetryCount() >= DLQ_THRESHOLD) {
            return "DLQ";
        }
        if (e.getRetryCount() != null && e.getRetryCount() > 0) {
            return "RETRYING";
        }
        return "PENDING";
    }

    private String normaliseStatus(String raw) {
        if (raw == null || raw.isBlank() || "all".equalsIgnoreCase(raw)) return null;
        String upper = raw.toUpperCase();
        return switch (upper) {
            case "PENDING", "PUBLISHED", "DLQ", "RETRYING" -> upper;
            default -> null;
        };
    }

    @SuppressWarnings("unused")
    private Duration since(LocalDateTime t) {
        return t == null ? Duration.ZERO : Duration.between(t, LocalDateTime.now());
    }
}
