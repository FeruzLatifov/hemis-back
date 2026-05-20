package uz.hemis.service.webhook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import uz.hemis.domain.entity.webhook.WebhookTarget;
import uz.hemis.domain.repository.webhook.WebhookTargetRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link WebhookFanoutConsumer} unit testlar — domain event → per-OTM Kafka fanout.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookFanoutConsumer")
class WebhookFanoutConsumerTest {

    @Mock private WebhookTargetRepository targetRepository;
    @SuppressWarnings("rawtypes")
    @Mock private KafkaTemplate kafkaTemplate;
    @Mock private Acknowledgment ack;

    private WebhookFanoutConsumer consumer;
    private ObjectMapper realObjectMapper;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Jackson + JSR-310 module — WebhookEventEnvelope.occurredAt = LocalDateTime.
        realObjectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        consumer = new WebhookFanoutConsumer(targetRepository, kafkaTemplate, realObjectMapper);
    }

    @Test
    @DisplayName("aktiv OTM yo'q — ack qilinadi, Kafka chaqirilmaydi")
    void noActiveTargets_acksAndSkips() {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of());
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "hemis.classifier.events.v1", 0, 0L, "agg-1", "{\"data\":\"x\"}");

        consumer.consume(record, ack);

        verify(ack).acknowledge();
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("3 ta aktiv OTM — har bir uchun alohida message, ack qilinadi")
    void threeActiveTargets_threeMessages() {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(
                target("337"), target("401"), target("501")));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "hemis.classifier.events.v1", 0, 0L, "agg-1", "{\"name\":\"new\"}");

        consumer.consume(record, ack);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> envCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(3)).send(eq("hemis.webhook.events"),
                keyCaptor.capture(), envCaptor.capture());

        assertThat(keyCaptor.getAllValues()).containsExactly("337", "401", "501");
        // Envelope tekshirish (1-message)
        String envelopeJson = envCaptor.getAllValues().get(0);
        assertThat(envelopeJson).contains("classifier.updated");
        assertThat(envelopeJson).contains("agg-1");
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("bir OTM Kafka send fail bo'lsa, qolgan OTMlar davom etadi")
    void oneOtmFails_othersContinue() {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(
                target("337"), target("401"), target("501")));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "hemis.classifier.events.v1", 0, 0L, "agg-1", "{}");

        when(kafkaTemplate.send(anyString(), eq("401"), anyString()))
                .thenThrow(new RuntimeException("Broker down for 401"));

        consumer.consume(record, ack);

        // 3 ta urinish (har OTM uchun), 1 ta exception — barchasi davom etadi
        verify(kafkaTemplate, times(3)).send(anyString(), anyString(), anyString());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("envelope format — Univer kontrakt fields")
    void envelopeStructure() throws Exception {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(target("337")));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "hemis.classifier.events.v1", 0, 0L, "cls-123", "{\"code\":\"NEW_CODE\"}");

        consumer.consume(record, ack);

        ArgumentCaptor<String> envCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("hemis.webhook.events"), eq("337"), envCaptor.capture());

        Map<String, Object> envelope = realObjectMapper.readValue(envCaptor.getValue(),
                new TypeReference<>() {});

        // WebhookEventEnvelope uses snake_case wire format (Univer kontrakt).
        assertThat(envelope).containsKey("event_id");
        assertThat(envelope).containsEntry("event_type", "classifier.updated");
        assertThat(envelope).containsEntry("aggregate_type", "classifier");
        assertThat(envelope).containsEntry("aggregate_id", "cls-123");
        assertThat(envelope).containsEntry("schema_version", 1);
        assertThat(envelope).containsKey("occurred_at");
        assertThat(envelope).containsKey("data");
    }

    @Test
    @DisplayName("aggregateType parse — hemis.rule.events.v1 → 'rule'")
    void ruleTopic_correctAggregateType() throws Exception {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(target("337")));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "hemis.rule.events.v1", 0, 0L, "rule-7", "{\"action\":\"created\"}");

        consumer.consume(record, ack);

        ArgumentCaptor<String> envCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("hemis.webhook.events"), eq("337"), envCaptor.capture());

        Map<String, Object> envelope = realObjectMapper.readValue(envCaptor.getValue(),
                new TypeReference<>() {});
        assertThat(envelope).containsEntry("aggregate_type", "rule");
        assertThat(envelope).containsEntry("event_type", "rule.updated");
    }

    @Test
    @DisplayName("invalid JSON payload — top-level exception, ack qilinmaydi (Kafka retry)")
    void invalidJsonPayload_noAck() {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(target("337")));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "hemis.classifier.events.v1", 0, 0L, "agg-1", "INVALID-JSON-{");

        consumer.consume(record, ack);

        // ack chaqirilmaydi — Kafka qayta beradi
        verify(ack, never()).acknowledge();
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    // =====================================================
    // helpers
    // =====================================================

    private static WebhookTarget target(String universityCode) {
        WebhookTarget t = new WebhookTarget();
        t.setUniversityCode(universityCode);
        return t;
    }
}
