package uz.hemis.service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.outbox.OutboxEvent;
import uz.hemis.domain.repository.outbox.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transactional Outbox publisher — programmatic API.
 *
 * <p>Boshqa servislar (ClassifierService, RuleService, …) shu Bean'ni
 * inject qilib, event yozadi. Outbox row asosiy domain entity bilan
 * <strong>bir transactionda</strong> yoziladi (atomicity kafolatlangan).</p>
 *
 * <p><strong>Misol — ClassifierService ichida:</strong></p>
 * <pre>
 * {@code
 * @Transactional
 * public ClassifierDto create(ClassifierCreateDto dto) {
 *     Classifier saved = repository.save(mapper.toEntity(dto));
 *     outboxPublisher.publish(
 *         "classifier", saved.getId().toString(), "created",
 *         new ClassifierCreatedEvent(saved.getId(), saved.getName(), ...)
 *     );
 *     return mapper.toDto(saved);
 * }
 * }
 * </pre>
 *
 * <p>Background {@link OutboxPoller} keyin row'ni Kafka topic'ga jo'natadi.</p>
 *
 * @since ADR-0007 / ADR-0010
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Event yozish (parent transaction'da). Asosiy entity {@code save()} dan keyin chaqirilishi
     * tavsiya etiladi — bir xil transactionda ikkalasi ham commit/rollback bo'ladi.
     *
     * @param aggregateType  V014 CHECK constraint: employee, employee_job, student, teacher,
     *                       classifier, university, building, audit
     * @param aggregateId    domain entity ID (UUID/string)
     * @param eventType      V014 CHECK constraint: created, updated, deleted, synced,
     *                       soft_deleted, restored, conflict_resolved
     * @param payload        Event body — JSON serialize qilinadi (record/DTO afzal)
     * @return outbox row ID (audit/correlation)
     * @throws OutboxPublishException  agar JSON serialization xato bo'lsa
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID publish(String aggregateType, String aggregateId, String eventType, Object payload) {
        return publish(aggregateType, aggregateId, eventType, payload, currentCorrelationId(), null);
    }

    /**
     * Event yozish — correlation/causation tracking bilan (distributed tracing).
     *
     * @param correlationId  request-level trace ID (HTTP X-Correlation-ID header)
     * @param causationId    avvalgi event ID (event chain)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID publish(
            String aggregateType,
            String aggregateId,
            String eventType,
            Object payload,
            String correlationId,
            String causationId
    ) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setSchemaVersion(1);
        event.setOccurredAt(LocalDateTime.now());
        event.setCorrelationId(correlationId);
        event.setCausationId(causationId);
        event.setCreatedBy(currentUsername());

        try {
            event.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new OutboxPublishException(
                    "Failed to serialize event payload for aggregate=" + aggregateType + "/" + aggregateId, e
            );
        }

        OutboxEvent saved = repository.save(event);
        log.debug("Outbox event queued: id={}, type={}, aggregate={}/{}",
                saved.getId(), eventType, aggregateType, aggregateId);
        return saved.getId();
    }

    private static String currentCorrelationId() {
        String fromMdc = MDC.get("correlationId");
        if (fromMdc != null && !fromMdc.isBlank()) return fromMdc;
        return MDC.get("traceId");  // Micrometer Observation default
    }

    private static String currentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication() != null
                    ? SecurityContextHolder.getContext().getAuthentication().getName()
                    : "system";
        } catch (Exception ignored) {
            return "system";
        }
    }
}
