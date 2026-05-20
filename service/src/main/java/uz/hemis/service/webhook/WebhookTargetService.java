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
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.webhook.WebhookDeliveryLog;
import uz.hemis.domain.entity.webhook.WebhookDeliveryStatus;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.webhook.WebhookDeliveryLogRepository;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final UniversityRepository universityRepository;
    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final WebhookSecretService secretService;
    private final WebhookSecretVault secretVault;
    private final WebhookDispatcher dispatcher;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${hemis.webhook.callback.protocol:https}")
    private String callbackProtocol;

    @Value("${hemis.webhook.callback.suffix:/rest/v1/hemis-callback/event}")
    private String callbackSuffix;

    // =====================================================
    // CRUD
    // =====================================================

    public List<WebhookTargetDto> findAll() {
        // N+1 hal: avval barcha target'larni, keyin university'larni bitta IN-clause
        // bilan oldindan yuklab, Map orqali O(1) lookup. Avval 1 + N findById edi.
        List<WebhookTarget> targets = targetRepository.findAll();
        if (targets.isEmpty()) return List.of();

        Set<String> codes = targets.stream()
                .map(WebhookTarget::getUniversityCode)
                .collect(Collectors.toSet());
        Map<String, University> universityByCode = new HashMap<>();
        universityRepository.findAllById(codes).forEach(u -> universityByCode.put(u.getCode(), u));

        return targets.stream()
                .map(t -> toDto(t, universityByCode.get(t.getUniversityCode())))
                .toList();
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
        target.setDescription(request.description());
        target.setSecretHash(secretService.hash(plainSecret));
        target.setTimeoutMs(request.timeoutMs() != null ? request.timeoutMs() : 30000);
        target.setMaxRetries(request.maxRetries() != null ? request.maxRetries() : 3);

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

        if (request.description() != null) target.setDescription(request.description());
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

        University university = universityRepository.findById(target.getUniversityCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "University: " + target.getUniversityCode()));
        String callbackUrl = buildCallbackUrl(university);
        if (callbackUrl == null) {
            throw new IllegalStateException(
                    "University " + target.getUniversityCode() + " has no student_url — cannot dispatch");
        }
        dispatcher.dispatchWithRetry(target, callbackUrl, testEventId, testEventType, envelopeJson, 1);

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

    /**
     * Bitta target uchun convenience — University ni alohida fetch qiladi (1 query).
     * Bulk path'lar (findAll) {@link #toDto(WebhookTarget, University)} ni ishlatadi.
     */
    private WebhookTargetDto toDto(WebhookTarget t) {
        University u = universityRepository.findById(t.getUniversityCode()).orElse(null);
        return toDto(t, u);
    }

    private WebhookTargetDto toDto(WebhookTarget t, University university) {
        // URL + active university'dan derive (2026-05-18 refactor)
        String callbackUrl = university != null ? buildCallbackUrl(university) : null;
        Boolean active = university != null
                && Boolean.TRUE.equals(university.getActive())
                && university.getDeleteTs() == null;

        return new WebhookTargetDto(
                t.getId(),
                t.getUniversityCode(),
                callbackUrl,
                t.getDescription(),
                active,
                t.getTimeoutMs(),
                t.getMaxRetries(),
                t.getCreatedAt(),
                t.getCreatedBy(),
                t.getUpdatedAt(),
                t.getUpdatedBy()
        );
    }

    /** Callback URL build: protocol + university.student_url + suffix (convention). */
    private String buildCallbackUrl(University university) {
        String studentUrl = university.getStudentUrl();
        if (studentUrl == null || studentUrl.isBlank()) {
            return null;
        }
        String host = studentUrl.replaceFirst("^https?://", "").replaceAll("/$", "");
        return callbackProtocol + "://" + host + callbackSuffix;
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
