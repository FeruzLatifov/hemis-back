package uz.hemis.service.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.hemis.domain.entity.webhook.WebhookDeliveryLog;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.webhook.WebhookDeliveryLogRepository;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Webhook dispatcher — Kafka {@code hemis.webhook.events} → REST POST to Univer.
 *
 * <p><strong>Flow:</strong></p>
 * <ol>
 *   <li>Kafka message (key=university_code, value=envelope JSON)</li>
 *   <li>WebhookTarget lookup (URL, secret, timeout)</li>
 *   <li>HMAC signature hisoblash</li>
 *   <li>REST POST → Univer endpoint</li>
 *   <li>Response asosida: success / failed / retry / dlq</li>
 *   <li>WebhookDeliveryLog yozish</li>
 * </ol>
 *
 * <p><strong>Retry policy (manual, Spring Retry'siz):</strong></p>
 * <ul>
 *   <li>Network error (timeout, connection refused) → RETRY</li>
 *   <li>HTTP 5xx → RETRY</li>
 *   <li>HTTP 4xx → FAILED (terminal — Univer payload xato deb hisoblaydi)</li>
 *   <li>HTTP 2xx → SUCCESS</li>
 *   <li>attempt &gt;= max_retries → DLQ</li>
 * </ul>
 *
 * <p>RETRY status'i {@code next_retry_at} bilan saqlanadi. {@link WebhookRetryScheduler}
 * (alohida class) due retry'larni qayta ishlaydi.</p>
 *
 * @since ADR-0012
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "hemis.webhook.enabled", havingValue = "true", matchIfMissing = true)
public class WebhookDispatcher {

    private final WebhookTargetRepository targetRepository;
    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final HmacSigner hmacSigner;
    private final WebhookSecretVault secretVault;
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WebhookMetrics metrics;

    @Value("${hemis.webhook.retry.max-attempts:5}")
    private int maxAttempts;

    @Value("${hemis.webhook.retry.initial-interval-ms:1000}")
    private long initialIntervalMs;

    @Value("${hemis.webhook.retry.multiplier:5}")
    private double multiplier;

    @Value("${hemis.webhook.retry.max-interval-ms:3600000}")
    private long maxIntervalMs;

    @Value("${hemis.webhook.dispatcher.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${hemis.webhook.dispatcher.read-timeout-ms:30000}")
    private int readTimeoutMs;

    @Value("${hemis.webhook.dispatcher.max-response-body-bytes:4096}")
    private int maxResponseBodyBytes;

    @Value("${hemis.webhook.topics.dlq:hemis.webhook.dlq}")
    private String dlqTopic;

    @KafkaListener(
            topics = "${hemis.webhook.topics.events:hemis.webhook.events}",
            groupId = "hemis-webhook-dispatcher",
            concurrency = "5"  // 5 ta parallel consumer (224 OTM uchun)
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String universityCode = record.key();
        String envelopeJson = record.value();

        try {
            // 1. Envelope parse
            JsonNode envelope = objectMapper.readTree(envelopeJson);
            UUID eventId = UUID.fromString(envelope.get("event_id").asText());
            String eventType = envelope.get("event_type").asText();

            // 2. Target lookup
            WebhookTarget target = targetRepository.findByUniversityCodeAndActiveTrue(universityCode)
                    .orElse(null);

            if (target == null) {
                log.debug("No active target for {} — event {} skipped", universityCode, eventId);
                ack.acknowledge();
                return;
            }

            // 3. Dispatch
            dispatchWithRetry(target, eventId, eventType, envelopeJson, 1);

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Dispatcher failed for university={}", universityCode, e);
            // ack qilmaymiz — Kafka retry
        }
    }

    /**
     * Bir dispatch attempt + result asosida status update.
     *
     * <p>Public — {@link WebhookRetryScheduler} ham chaqirishi mumkin (retry).</p>
     */
    public void dispatchWithRetry(
            WebhookTarget target,
            UUID eventId,
            String eventType,
            String envelopeJson,
            int attemptN
    ) {
        WebhookDeliveryLog logEntry = new WebhookDeliveryLog();
        logEntry.setEventId(eventId);
        logEntry.setEventType(eventType);
        logEntry.setTarget(target);
        logEntry.setUniversityCode(target.getUniversityCode());
        logEntry.setAttemptN(attemptN);
        logEntry.setDispatchedAt(LocalDateTime.now());

        long startNs = System.nanoTime();

        try {
            ResponseEntity<String> response = doHttpPost(target, envelopeJson);
            int duration = (int) ((System.nanoTime() - startNs) / 1_000_000);

            logEntry.markSuccess(
                    response.getStatusCode().value(),
                    truncate(response.getBody()),
                    duration
            );
            metrics.recordSuccess(target.getUniversityCode(), Duration.ofMillis(duration));
            log.debug("Webhook delivered: event={} → {} (HTTP {} in {}ms)",
                    eventId, target.getUniversityCode(),
                    response.getStatusCode().value(), duration);

        } catch (HttpClientErrorException e) {
            // 4xx — terminal failure (payload xato)
            int duration = (int) ((System.nanoTime() - startNs) / 1_000_000);
            logEntry.markFailed(
                    e.getStatusCode().value(),
                    truncate(e.getResponseBodyAsString()),
                    e.getMessage()
            );
            metrics.recordFailed(target.getUniversityCode(), Duration.ofMillis(duration));
            log.warn("Webhook failed (4xx terminal): event={} → {} HTTP {}",
                    eventId, target.getUniversityCode(), e.getStatusCode().value());

        } catch (HttpServerErrorException | ResourceAccessException e) {
            // 5xx yoki network error — retry yoki DLQ
            handleRetryable(target, eventId, eventType, envelopeJson, attemptN, logEntry, e);

        } catch (Exception e) {
            // Unexpected error
            handleRetryable(target, eventId, eventType, envelopeJson, attemptN, logEntry, e);
        }

        deliveryLogRepository.save(logEntry);
    }

    private void handleRetryable(
            WebhookTarget target,
            UUID eventId,
            String eventType,
            String envelopeJson,
            int attemptN,
            WebhookDeliveryLog logEntry,
            Exception e
    ) {
        int targetMaxRetries = target.getMaxRetries() != null ? target.getMaxRetries() : maxAttempts;

        if (attemptN >= targetMaxRetries) {
            // DLQ — Kafka topic'ga yuborish + log
            logEntry.markDlq("Max retries exceeded: " + e.getMessage());
            sendToDlq(eventId, target.getUniversityCode(), envelopeJson, e.getMessage());
            metrics.recordDlq(target.getUniversityCode());
            log.error("Webhook DLQ: event={} → {} after {} attempts",
                    eventId, target.getUniversityCode(), attemptN);
        } else {
            // RETRY — exponential backoff
            LocalDateTime nextRetryAt = computeNextRetry(attemptN);
            Integer httpStatus = (e instanceof HttpServerErrorException hse)
                    ? hse.getStatusCode().value() : null;
            logEntry.markRetry(httpStatus, e.getMessage(), nextRetryAt);
            metrics.recordRetry(target.getUniversityCode());
            log.warn("Webhook retry scheduled: event={} → {} attempt={}/{} next={}",
                    eventId, target.getUniversityCode(), attemptN, targetMaxRetries, nextRetryAt);
        }
    }

    /** Exponential backoff: 1s, 5s, 30s, 5min, 1h (cap at max-interval-ms). */
    private LocalDateTime computeNextRetry(int attemptN) {
        long delayMs = (long) (initialIntervalMs * Math.pow(multiplier, attemptN - 1));
        delayMs = Math.min(delayMs, maxIntervalMs);
        return LocalDateTime.now().plus(Duration.ofMillis(delayMs));
    }

    private ResponseEntity<String> doHttpPost(WebhookTarget target, String body) {
        long timestamp = System.currentTimeMillis() / 1000;
        String plainSecret = secretVault.resolve(target);
        String signature = hmacSigner.sign(plainSecret, timestamp, body);

        RestClient client = restClientBuilder
                .requestFactory(buildRequestFactory(target.getTimeoutMs()))
                .build();

        return client.post()
                .uri(target.getCallbackUrl())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("X-Hemis-Signature", signature)
                .header("X-Hemis-Timestamp", String.valueOf(timestamp))
                .header("X-Hemis-University-Code", target.getUniversityCode())
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }

    private org.springframework.http.client.SimpleClientHttpRequestFactory buildRequestFactory(Integer timeoutMs) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        int readTimeout = (timeoutMs != null && timeoutMs > 0) ? timeoutMs : readTimeoutMs;
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    private void sendToDlq(UUID eventId, String universityCode, String envelopeJson, String errorMessage) {
        try {
            String dlqPayload = objectMapper.writeValueAsString(java.util.Map.of(
                    "event_id", eventId.toString(),
                    "university_code", universityCode,
                    "envelope", envelopeJson,
                    "error", errorMessage,
                    "moved_at", LocalDateTime.now().toString()
            ));
            kafkaTemplate.send(dlqTopic, universityCode, dlqPayload);
        } catch (Exception e) {
            log.error("Failed to send to DLQ topic {}: {}", dlqTopic, e.getMessage());
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        if (s.length() <= maxResponseBodyBytes) return s;
        return s.substring(0, maxResponseBodyBytes - 16) + "...[truncated]";
    }
}
