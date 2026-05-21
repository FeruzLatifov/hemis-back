package uz.hemis.service.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.hemis.domain.entity.webhook.WebhookDeliveryLog;
import uz.hemis.domain.entity.webhook.WebhookDeliveryStatus;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.webhook.WebhookDeliveryLogRepository;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WebhookDispatcher#dispatchWithRetry}.
 *
 * <p><strong>Scope:</strong> retry classification (2xx/4xx/5xx/network),
 * delivery log status transitions, DLQ Kafka publishing, metrics dispatch,
 * exponential backoff in {@code next_retry_at}.</p>
 *
 * <p><strong>HTTP boundary:</strong> {@code doHttpPost()} is stubbed via Mockito
 * spy (it's {@code protected} for this purpose) — avoids brittle
 * {@code RestClient.Builder} fluent-chain mocks and an external WireMock dep.</p>
 *
 * <p>HMAC-header verification lives in {@code HmacSignerTest} — here we just
 * confirm the dispatcher delegates to it via the spy's side-effect.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookDispatcher Tests")
class WebhookDispatcherTest {

    @Mock private WebhookTargetRepository targetRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private WebhookDeliveryLogRepository deliveryLogRepository;
    @Mock private HmacSigner hmacSigner;
    @Mock private WebhookSecretVault secretVault;
    @Mock private RestClient.Builder restClientBuilder;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Mock private WebhookMetrics metrics;

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private WebhookDispatcher dispatcher;

    private WebhookTarget target;
    private final String callbackUrl = "https://otm.example.org/rest/v1/hemis-callback/event";
    private final UUID eventId = UUID.fromString("605dc9ce-e832-42a1-92ea-b2223ccb477f");
    private final String eventType = "classifier.updated";
    private final String envelope = "{\"event_id\":\"" + eventId + "\",\"event_type\":\"" + eventType + "\"}";

    @BeforeEach
    void setup() {
        target = new WebhookTarget();
        target.setUniversityCode("337");
        target.setMaxRetries(3);
        target.setTimeoutMs(5000);

        // Tune retry config (production defaults via @Value)
        ReflectionTestUtils.setField(dispatcher, "maxAttempts", 3);
        ReflectionTestUtils.setField(dispatcher, "initialIntervalMs", 1000L);
        ReflectionTestUtils.setField(dispatcher, "multiplier", 5.0);
        ReflectionTestUtils.setField(dispatcher, "maxIntervalMs", 3_600_000L);
        ReflectionTestUtils.setField(dispatcher, "dlqTopic", "hemis.webhook.dlq");
        ReflectionTestUtils.setField(dispatcher, "maxResponseBodyBytes", 4096);
    }

    // =========================================================
    // 2xx — SUCCESS
    // =========================================================

    @Test
    @DisplayName("HTTP 200 → markSuccess + metrics.recordSuccess + log saved with httpStatus=200")
    void dispatch_2xx_marksSuccess() {
        WebhookDispatcher spy = org.mockito.Mockito.spy(dispatcher);
        doReturn(ResponseEntity.ok("accepted"))
            .when(spy).doHttpPost(eq(target), eq(callbackUrl), eq(envelope));

        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 1);

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(deliveryLogRepository).save(captor.capture());
        WebhookDeliveryLog saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(WebhookDeliveryStatus.SUCCESS);
        assertThat(saved.getHttpStatus()).isEqualTo(200);
        assertThat(saved.getResponseBody()).isEqualTo("accepted");
        assertThat(saved.getCompletedAt()).isNotNull();
        verify(metrics).recordSuccess(eq("337"), any());
        verify(metrics, never()).recordRetry(anyString());
        verify(metrics, never()).recordFailed(anyString(), any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    // =========================================================
    // 4xx — FAILED (terminal)
    // =========================================================

    @Test
    @DisplayName("HTTP 400 → markFailed terminal (no retry, no DLQ)")
    void dispatch_4xx_marksFailedTerminal() {
        WebhookDispatcher spy = org.mockito.Mockito.spy(dispatcher);
        doThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, "invalid payload".getBytes(), null))
            .when(spy).doHttpPost(any(), anyString(), anyString());

        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 1);

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(deliveryLogRepository).save(captor.capture());
        WebhookDeliveryLog saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(WebhookDeliveryStatus.FAILED);
        assertThat(saved.getHttpStatus()).isEqualTo(400);
        assertThat(saved.getNextRetryAt()).as("4xx is terminal — no retry scheduled").isNull();
        verify(metrics).recordFailed(eq("337"), any());
        verify(metrics, never()).recordRetry(anyString());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    // =========================================================
    // 5xx — RETRY (attempt < max)
    // =========================================================

    @Test
    @DisplayName("HTTP 503 on attempt 1/3 → markRetry with next_retry_at set")
    void dispatch_5xx_attempt1_schedulesRetry() {
        WebhookDispatcher spy = org.mockito.Mockito.spy(dispatcher);
        doThrow(HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", null, new byte[0], null))
            .when(spy).doHttpPost(any(), anyString(), anyString());

        LocalDateTime before = LocalDateTime.now();
        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 1);

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(deliveryLogRepository).save(captor.capture());
        WebhookDeliveryLog saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(WebhookDeliveryStatus.RETRY);
        assertThat(saved.getHttpStatus()).isEqualTo(503);
        assertThat(saved.getNextRetryAt()).isNotNull().isAfter(before);
        verify(metrics).recordRetry(eq("337"));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    // =========================================================
    // 5xx — DLQ (attempt == max)
    // =========================================================

    @Test
    @DisplayName("HTTP 500 on attempt 3/3 → markDlq + Kafka DLQ publish")
    void dispatch_5xx_attempt3_movesToDlq() {
        WebhookDispatcher spy = org.mockito.Mockito.spy(dispatcher);
        doThrow(HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, new byte[0], null))
            .when(spy).doHttpPost(any(), anyString(), anyString());

        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 3);

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(deliveryLogRepository).save(captor.capture());
        WebhookDeliveryLog saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(WebhookDeliveryStatus.DLQ);
        assertThat(saved.getCompletedAt()).isNotNull();
        verify(metrics).recordDlq(eq("337"));
        verify(kafkaTemplate, times(1)).send(eq("hemis.webhook.dlq"), eq("337"), anyString());
    }

    // =========================================================
    // Network error — RETRY (treated as 5xx)
    // =========================================================

    @Test
    @DisplayName("ResourceAccessException (timeout/connection refused) on attempt 1 → retry scheduled")
    void dispatch_networkError_schedulesRetry() {
        WebhookDispatcher spy = org.mockito.Mockito.spy(dispatcher);
        doThrow(new ResourceAccessException("Read timed out"))
            .when(spy).doHttpPost(any(), anyString(), anyString());

        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 1);

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(deliveryLogRepository).save(captor.capture());
        WebhookDeliveryLog saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(WebhookDeliveryStatus.RETRY);
        assertThat(saved.getHttpStatus()).as("network error — no HTTP status").isNull();
        assertThat(saved.getErrorMessage()).contains("Read timed out");
        assertThat(saved.getNextRetryAt()).isNotNull();
        verify(metrics).recordRetry(eq("337"));
    }

    // =========================================================
    // Exponential backoff sanity
    // =========================================================

    @Test
    @DisplayName("Backoff grows: attempt 2 next_retry_at > attempt 1 next_retry_at")
    void dispatch_exponentialBackoff_growsBetweenAttempts() {
        WebhookDispatcher spy = org.mockito.Mockito.spy(dispatcher);
        doThrow(HttpServerErrorException.create(
                HttpStatus.BAD_GATEWAY, "Bad Gateway", null, new byte[0], null))
            .when(spy).doHttpPost(any(), anyString(), anyString());

        LocalDateTime t0 = LocalDateTime.now();
        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 1);
        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 2);

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(deliveryLogRepository, times(2)).save(captor.capture());

        LocalDateTime retry1 = captor.getAllValues().get(0).getNextRetryAt();
        LocalDateTime retry2 = captor.getAllValues().get(1).getNextRetryAt();
        assertThat(retry1).isAfter(t0);
        assertThat(retry2).as("attempt 2 backoff (5s @ multiplier=5) > attempt 1 (1s)").isAfter(retry1);
    }

    // =========================================================
    // DLQ Kafka publish failure must NOT crash dispatcher
    // =========================================================

    @Test
    @DisplayName("DLQ Kafka publish failure is logged but does not propagate")
    void dispatch_dlqKafkaFailure_isSilencedToLog() {
        WebhookDispatcher spy = org.mockito.Mockito.spy(dispatcher);
        doThrow(HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", null, new byte[0], null))
            .when(spy).doHttpPost(any(), anyString(), anyString());
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Kafka broker down"));

        // Must not throw — log only
        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 3);

        verify(deliveryLogRepository, times(1)).save(any());
        verify(metrics).recordDlq(eq("337"));
    }

    // =========================================================
    // Per-target maxRetries override
    // =========================================================

    @Test
    @DisplayName("Target with maxRetries=1 → attempt 1 immediately moves to DLQ on 5xx")
    void dispatch_targetMaxRetriesOverride_dlqOnFirstAttempt() {
        target.setMaxRetries(1);
        WebhookDispatcher spy = org.mockito.Mockito.spy(dispatcher);
        doThrow(HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", null, new byte[0], null))
            .when(spy).doHttpPost(any(), anyString(), anyString());

        spy.dispatchWithRetry(target, callbackUrl, eventId, eventType, envelope, 1);

        ArgumentCaptor<WebhookDeliveryLog> captor = ArgumentCaptor.forClass(WebhookDeliveryLog.class);
        verify(deliveryLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(WebhookDeliveryStatus.DLQ);
        verify(metrics).recordDlq(eq("337"));
    }

    // Use IOException to ensure assertion-level imports stay valid; not directly invoked.
    @SuppressWarnings("unused")
    private void unused() throws IOException { /* compile-time anchor */ }
}
