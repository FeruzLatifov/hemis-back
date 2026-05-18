package uz.hemis.service.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.webhook.WebhookDeliveryLogDto;
import uz.hemis.common.dto.webhook.WebhookSecretResponse;
import uz.hemis.common.dto.webhook.WebhookTargetCreateRequest;
import uz.hemis.common.dto.webhook.WebhookTargetDto;
import uz.hemis.common.dto.webhook.WebhookTargetUpdateRequest;
import uz.hemis.common.exception.ConflictException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.webhook.WebhookDeliveryLog;
import uz.hemis.domain.entity.webhook.WebhookDeliveryStatus;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.webhook.WebhookDeliveryLogRepository;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

import java.util.List;
import java.util.UUID;

/**
 * Webhook target boshqaruv service layer.
 *
 * @since ADR-0012
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WebhookTargetService {

    private final WebhookTargetRepository targetRepository;
    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final WebhookSecretService secretService;
    private final WebhookSecretVault secretVault;
    private final WebhookDispatcher dispatcher;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // =====================================================
    // CRUD
    // =====================================================

    public List<WebhookTargetDto> findAll() {
        return targetRepository.findAll().stream().map(this::toDto).toList();
    }

    public WebhookTargetDto findById(UUID id) {
        return toDto(loadOrThrow(id));
    }

    public WebhookTargetDto findByUniversityCode(String universityCode) {
        return targetRepository.findByUniversityCode(universityCode)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "WebhookTarget for university: " + universityCode));
    }

    /**
     * Yangi target qo'shish + secret generate.
     *
     * <p>Plain secret response orqali bir marta qaytariladi. OTM IT
     * uni {@code .env}'ga yozishi shart.</p>
     */
    @Transactional
    public WebhookSecretResponse create(WebhookTargetCreateRequest request) {
        if (targetRepository.existsByUniversityCode(request.universityCode())) {
            throw new ConflictException(
                    "Webhook target already exists for university: " + request.universityCode());
        }

        String plainSecret = secretService.generatePlainSecret();

        WebhookTarget target = new WebhookTarget();
        target.setUniversityCode(request.universityCode());
        target.setCallbackUrl(request.callbackUrl());
        target.setDescription(request.description());
        target.setSecretHash(secretService.hash(plainSecret));
        target.setActive(true);
        target.setTimeoutMs(request.timeoutMs() != null ? request.timeoutMs() : 30000);
        target.setMaxRetries(request.maxRetries() != null ? request.maxRetries() : 5);

        WebhookTarget saved = targetRepository.save(target);

        // In-memory vault — dispatcher signature uchun ishlatadi
        secretVault.store(saved.getUniversityCode(), plainSecret);

        log.info("Webhook target created: university={}, id={}", saved.getUniversityCode(), saved.getId());
        return WebhookSecretResponse.create(saved.getId(), saved.getUniversityCode(), plainSecret);
    }

    /**
     * Target update (partial).
     */
    @Transactional
    public WebhookTargetDto update(UUID id, WebhookTargetUpdateRequest request) {
        WebhookTarget target = loadOrThrow(id);

        if (request.callbackUrl() != null) target.setCallbackUrl(request.callbackUrl());
        if (request.description() != null) target.setDescription(request.description());
        if (request.active() != null) target.setActive(request.active());
        if (request.timeoutMs() != null) target.setTimeoutMs(request.timeoutMs());
        if (request.maxRetries() != null) target.setMaxRetries(request.maxRetries());

        log.info("Webhook target updated: id={}, university={}", id, target.getUniversityCode());
        return toDto(target);
    }

    /**
     * Soft delete (deleted_at o'rnatish — @SQLRestriction tomonidan filter qilinadi).
     */
    @Transactional
    public void delete(UUID id) {
        WebhookTarget target = loadOrThrow(id);
        target.softDelete();
        secretVault.remove(target.getUniversityCode());
        log.warn("Webhook target soft-deleted: id={}, university={}", id, target.getUniversityCode());
    }

    /**
     * Secret rotation — yangi plain secret yaratish, eski hash'ni almashtirish.
     *
     * <p>Eski secret'ni OTM hali ishlatishi mumkin — admin OTM IT bilan koordinatsiya
     * qilishi kerak (yangi secret'ni .env'ga yozish + service restart).</p>
     */
    @Transactional
    public WebhookSecretResponse regenerateSecret(UUID id) {
        WebhookTarget target = loadOrThrow(id);
        String plainSecret = secretService.generatePlainSecret();
        target.setSecretHash(secretService.hash(plainSecret));

        secretVault.store(target.getUniversityCode(), plainSecret);

        log.warn("Webhook secret regenerated: id={}, university={} — Univer .env update required",
                id, target.getUniversityCode());
        return WebhookSecretResponse.create(target.getId(), target.getUniversityCode(), plainSecret);
    }

    // =====================================================
    // Delivery log views
    // =====================================================

    public Page<WebhookDeliveryLogDto> findDeliveriesByTarget(UUID targetId, Pageable pageable) {
        loadOrThrow(targetId);  // existence check
        return deliveryLogRepository.findByTargetIdOrderByDispatchedAtDesc(targetId, pageable)
                .map(this::toLogDto);
    }

    public List<WebhookDeliveryLogDto> findDeliveriesByEvent(UUID eventId) {
        return deliveryLogRepository.findByEventIdOrderByAttemptNAsc(eventId).stream()
                .map(this::toLogDto)
                .toList();
    }

    public Page<WebhookDeliveryLogDto> findDlqEntries(Pageable pageable) {
        return deliveryLogRepository
                .findByStatusOrderByDispatchedAtDesc(WebhookDeliveryStatus.DLQ, pageable)
                .map(this::toLogDto);
    }

    // =====================================================
    // Sandbox — manual test event
    // =====================================================

    /**
     * Sandbox test — bitta target'ga synthetic event yuborish.
     *
     * <p>Admin UI'dan "Test webhook" tugmasi orqali ishlatiladi. Real outbox/Kafka
     * yo'l o'rniga to'g'ridan-to'g'ri {@link WebhookDispatcher#dispatchWithRetry} chaqiradi.</p>
     */
    @Transactional
    public WebhookDeliveryLogDto sendTestEvent(UUID targetId) {
        WebhookTarget target = loadOrThrow(targetId);

        UUID testEventId = UUID.randomUUID();
        String testEventType = "webhook.test";

        WebhookEventEnvelope envelope = new WebhookEventEnvelope(
                testEventId,
                testEventType,
                "webhook",
                "test-" + testEventId,
                java.time.LocalDateTime.now(),
                1,
                java.util.Map.of(
                        "message", "Test webhook from HEMIS admin sandbox",
                        "timestamp", System.currentTimeMillis()
                )
        );

        String envelopeJson;
        try {
            envelopeJson = objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize test envelope", e);
        }

        log.info("Sandbox webhook test: target={} ({}), eventId={}",
                target.getUniversityCode(), targetId, testEventId);

        dispatcher.dispatchWithRetry(target, testEventId, testEventType, envelopeJson, 1);

        // Eng so'nggi log entry'ni qaytarish (so'rovchi natijani ko'rishi uchun)
        return deliveryLogRepository
                .findByEventIdOrderByAttemptNAsc(testEventId).stream()
                .findFirst()
                .map(this::toLogDto)
                .orElseThrow(() -> new IllegalStateException("Test delivery log not persisted"));
    }

    // =====================================================
    // Internal
    // =====================================================

    private WebhookTarget loadOrThrow(UUID id) {
        return targetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookTarget: " + id));
    }

    private WebhookTargetDto toDto(WebhookTarget t) {
        return new WebhookTargetDto(
                t.getId(),
                t.getUniversityCode(),
                t.getCallbackUrl(),
                t.getDescription(),
                t.getActive(),
                t.getTimeoutMs(),
                t.getMaxRetries(),
                t.getCreatedAt(),
                t.getCreatedBy(),
                t.getUpdatedAt(),
                t.getUpdatedBy()
        );
    }

    private WebhookDeliveryLogDto toLogDto(WebhookDeliveryLog l) {
        return new WebhookDeliveryLogDto(
                l.getId(),
                l.getEventId(),
                l.getEventType(),
                l.getUniversityCode(),
                l.getAttemptN(),
                l.getHttpStatus(),
                l.getResponseBody(),
                l.getErrorMessage(),
                l.getDurationMs(),
                l.getStatus() != null ? l.getStatus().getDbValue() : null,
                l.getDispatchedAt(),
                l.getCompletedAt(),
                l.getNextRetryAt()
        );
    }
}
