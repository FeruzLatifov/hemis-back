package uz.hemis.service.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.webhook.WebhookAckRequest;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.domain.entity.webhook.WebhookApplyResult;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.webhook.WebhookApplyResultRepository;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Univer → markaz apply-status ack qabul qilish (K2).
 *
 * <p>"Delivered != applied" gap'ini yopadi. Univer {@code ApplyHemisEventJob} event'ni apply
 * qilgach markazga HMAC-imzolangan ack POST qiladi (inbound webhook'ning teskarisi — bir xil
 * {@code X-Hemis-Signature/Timestamp/University-Code} sxema). Markaz secret_enc (K1) bilan
 * verify qiladi va natijani {@code webhook_apply_result}'ga upsert qiladi.</p>
 *
 * @since ADR-0012 (K2)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookAckService {

    private static final long TIMESTAMP_WINDOW_SEC = 300;  // 5 daqiqa replay oynasi (inbound bilan bir xil)
    private static final int ERROR_MAX = 2000;

    private final WebhookTargetRepository targetRepository;
    private final WebhookSecretVault secretVault;
    private final HmacSigner hmacSigner;
    private final WebhookApplyResultRepository applyResultRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processAck(String universityCode, String signature, String timestampHeader, String rawBody) {
        verifySignature(universityCode, signature, timestampHeader, rawBody);

        WebhookAckRequest ack = parse(rawBody);
        UUID eventId = parseEventId(ack.eventId());
        String status = ack.status();
        if (!"applied".equals(status) && !"failed".equals(status)) {
            throw new BadRequestException("status 'applied' yoki 'failed' bo'lishi kerak: " + status);
        }

        WebhookApplyResult result = applyResultRepository
                .findByEventIdAndUniversityCode(eventId, universityCode)
                .orElseGet(WebhookApplyResult::new);
        LocalDateTime now = LocalDateTime.now();
        result.setEventId(eventId);
        result.setUniversityCode(universityCode);
        result.setStatus(status);
        result.setAppliedAt("applied".equals(status) ? now : null);
        result.setErrorMessage(truncate(ack.errorMessage()));
        result.setReportedAt(now);
        applyResultRepository.save(result);

        log.info("Webhook apply ack: university={} event={} status={}", universityCode, eventId, status);
    }

    private void verifySignature(String universityCode, String signature, String timestampHeader, String rawBody) {
        if (universityCode == null || signature == null || timestampHeader == null) {
            throw new AccessDeniedException("Missing X-Hemis-University-Code/Signature/Timestamp header");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader);
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("Invalid X-Hemis-Timestamp");
        }
        if (Math.abs(Instant.now().getEpochSecond() - timestamp) > TIMESTAMP_WINDOW_SEC) {
            throw new AccessDeniedException("Stale timestamp (>5 min)");
        }
        WebhookTarget target = targetRepository.findByUniversityCode(universityCode)
                .orElseThrow(() -> new AccessDeniedException("Unknown university: " + universityCode));
        String secret;
        try {
            secret = secretVault.resolve(target);
        } catch (WebhookSecretVault.WebhookSecretMissingException e) {
            throw new AccessDeniedException("Webhook secret unavailable for " + universityCode);
        }
        String expected = hmacSigner.sign(secret, timestamp, rawBody);
        if (!hmacSigner.verify(expected, signature)) {
            log.warn("Webhook ack invalid signature: university={}", universityCode);
            throw new AccessDeniedException("Invalid signature");
        }
    }

    private WebhookAckRequest parse(String rawBody) {
        try {
            WebhookAckRequest ack = objectMapper.readValue(rawBody, WebhookAckRequest.class);
            if (ack.eventId() == null || ack.status() == null) {
                throw new BadRequestException("event_id va status majburiy");
            }
            return ack;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Yaroqsiz ack JSON: " + e.getMessage());
        }
    }

    private UUID parseEventId(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("event_id UUID bo'lishi kerak: " + s);
        }
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > ERROR_MAX ? s.substring(0, ERROR_MAX) + "...[truncated]" : s;
    }
}
