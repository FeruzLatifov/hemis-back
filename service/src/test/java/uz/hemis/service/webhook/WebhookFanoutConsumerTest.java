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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link WebhookFanoutConsumer} unit testlar — domain event → per-OTM Kafka fanout.
 *
 * <p><strong>Kontrakt (SYNC-WH-03, 83d98de):</strong> ack faqat BARCHA OTM send'i broker
 * tomonidan tasdiqlangandan keyin qilinadi. Bittasi fail bo'lsa — ack YO'Q, Kafka butun
 * event'ni qayta beradi (at-least-once). Shuning uchun {@code kafkaTemplate.send(...)}
 * qaytargan future testlarda ham stub qilinishi shart.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookFanoutConsumer")
class WebhookFanoutConsumerTest {

    /** {@code OutboxPoller} wire-contract header (SYNC-WH-02 deterministik event_id). */
    private static final String HEADER_EVENT_ID = "hemis-event-id";

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
        ConsumerRecord<String, String> record = record("hemis.classifier.events.v1", "agg-1", "{\"data\":\"x\"}");

        consumer.consume(record, ack);

        verify(ack).acknowledge();
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("3 ta aktiv OTM — har bir uchun alohida message, ack qilinadi")
    void threeActiveTargets_threeMessages() {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(
                target("337"), target("401"), target("501")));
        stubSendOk();
        ConsumerRecord<String, String> record = record("hemis.classifier.events.v1", "agg-1", "{\"name\":\"new\"}");

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
    @DisplayName("bir OTM send'i future'da fail — qolgan OTMlar yuboriladi, ack QILINMAYDI (SYNC-WH-03)")
    void oneOtmFutureFails_othersSentButNoAck() {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(
                target("337"), target("401"), target("501")));
        stubSendOk();
        stubSendFailure("401", new RuntimeException("Broker down for 401"));
        ConsumerRecord<String, String> record = record("hemis.classifier.events.v1", "agg-1", "{}");

        consumer.consume(record, ack);

        // Bir OTM'ning nosozligi qolgan OTMlarga yuborishni bloklamaydi — 3 ta urinish.
        verify(kafkaTemplate, times(3)).send(anyString(), anyString(), anyString());
        // Lekin ack YO'Q: aks holda offset commit bo'lib, 401 event'ni umuman olmay qolardi
        // (jimgina yo'qotish). ack qilmasak Kafka butun event'ni qayta beradi; event_id
        // deterministik bo'lgani uchun Univer takrorlarni dedup qiladi.
        verify(ack, never()).acknowledge();
    }

    @Test
    @DisplayName("send sinxron exception tashlasa — ack QILINMAYDI (Kafka retry)")
    void sendThrowsSynchronously_noAck() {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(
                target("337"), target("401")));
        stubSendOk();
        when(kafkaTemplate.send(anyString(), eq("401"), anyString()))
                .thenThrow(new IllegalStateException("Producer closed"));
        ConsumerRecord<String, String> record = record("hemis.classifier.events.v1", "agg-1", "{}");

        consumer.consume(record, ack);

        verify(ack, never()).acknowledge();
    }

    @Test
    @DisplayName("envelope format — Univer kontrakt fields")
    void envelopeStructure() throws Exception {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(target("337")));
        stubSendOk();
        ConsumerRecord<String, String> record = record("hemis.classifier.events.v1", "cls-123", "{\"code\":\"NEW_CODE\"}");

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
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("aggregateType parse — hemis.rule.events.v1 → 'rule'")
    void ruleTopic_correctAggregateType() throws Exception {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(target("337")));
        stubSendOk();
        ConsumerRecord<String, String> record = record("hemis.rule.events.v1", "rule-7", "{\"action\":\"created\"}");

        consumer.consume(record, ack);

        ArgumentCaptor<String> envCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("hemis.webhook.events"), eq("337"), envCaptor.capture());

        Map<String, Object> envelope = realObjectMapper.readValue(envCaptor.getValue(),
                new TypeReference<>() {});
        assertThat(envelope).containsEntry("aggregate_type", "rule");
        assertThat(envelope).containsEntry("event_type", "rule.updated");
    }

    @Test
    @DisplayName("event_id — outbox 'hemis-event-id' header'idan olinadi (SYNC-WH-02, random EMAS)")
    void eventId_takenFromOutboxHeader() throws Exception {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(target("337")));
        stubSendOk();
        UUID outboxEventId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        ConsumerRecord<String, String> record = record("hemis.classifier.events.v1", "cls-9", "{\"code\":\"C\"}");
        record.headers().add(HEADER_EVENT_ID, outboxEventId.toString().getBytes(StandardCharsets.UTF_8));

        consumer.consume(record, ack);

        assertThat(eventIdOf(captureEnvelope())).isEqualTo(outboxEventId.toString());
    }

    @Test
    @DisplayName("header yo'q — event_id mazmundan deterministik (redelivery'da bir xil)")
    void eventId_deterministicWithoutHeader() throws Exception {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(target("337")));
        stubSendOk();

        consumer.consume(record("hemis.classifier.events.v1", "cls-9", "{\"code\":\"C\"}"), ack);
        consumer.consume(record("hemis.classifier.events.v1", "cls-9", "{\"code\":\"C\"}"), ack);

        ArgumentCaptor<String> envCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(2)).send(eq("hemis.webhook.events"), eq("337"), envCaptor.capture());

        List<String> envelopes = envCaptor.getAllValues();
        assertThat(eventIdOf(parse(envelopes.get(0))))
                .isEqualTo(eventIdOf(parse(envelopes.get(1))));
    }

    @Test
    @DisplayName("buzuq JSON payload — send ham, ack ham YO'Q (Kafka retry)")
    void invalidJsonPayload_noAck() {
        when(targetRepository.findAllForActiveUniversities()).thenReturn(List.of(target("337")));
        ConsumerRecord<String, String> record = record("hemis.classifier.events.v1", "agg-1", "INVALID-JSON-{");

        consumer.consume(record, ack);

        // Envelope qurilishi payload parse'dan boshlanadi — buzuq payload birorta OTMga
        // chiqib ketmasligi shart (poison message Univer'ga tarqalmasin), ack ham qilinmaydi.
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
        verify(ack, never()).acknowledge();
    }

    // =====================================================
    // helpers
    // =====================================================

    /** Kafka send muvaffaqiyatli tasdiqlangan future qaytaradi (kod ack'dan oldin kutadi). */
    @SuppressWarnings("unchecked")
    private void stubSendOk() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    /** Berilgan OTM uchun send future'i exception bilan yopiladi (broker rad etdi). */
    @SuppressWarnings("unchecked")
    private void stubSendFailure(String universityCode, Throwable cause) {
        when(kafkaTemplate.send(anyString(), eq(universityCode), anyString()))
                .thenReturn(CompletableFuture.failedFuture(cause));
    }

    private static ConsumerRecord<String, String> record(String topic, String key, String value) {
        return new ConsumerRecord<>(topic, 0, 0L, key, value);
    }

    private Map<String, Object> captureEnvelope() throws Exception {
        ArgumentCaptor<String> envCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("hemis.webhook.events"), eq("337"), envCaptor.capture());
        return parse(envCaptor.getValue());
    }

    private Map<String, Object> parse(String envelopeJson) throws Exception {
        return realObjectMapper.readValue(envelopeJson, new TypeReference<>() {});
    }

    private static String eventIdOf(Map<String, Object> envelope) {
        return String.valueOf(envelope.get("event_id"));
    }

    private static WebhookTarget target(String universityCode) {
        WebhookTarget t = new WebhookTarget();
        t.setUniversityCode(universityCode);
        return t;
    }
}
