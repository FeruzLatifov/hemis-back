package uz.hemis.service.outbox;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import uz.hemis.domain.entity.outbox.OutboxEvent;
import uz.hemis.domain.repository.outbox.OutboxEventRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxPoller — DB → Kafka transactional outbox dispatcher")
class OutboxPollerTest {

    /** Wire-contract header — downstream consumer deterministik event_id sifatida ishlatadi. */
    private static final String EVENT_ID_HEADER = "hemis-event-id";

    @Mock private OutboxEventRepository repository;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxPoller poller;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(poller, "batchSize", 100);
        ReflectionTestUtils.setField(poller, "retentionDays", 30);
    }

    private OutboxEvent buildEvent() {
        OutboxEvent e = new OutboxEvent();
        e.setId(UUID.randomUUID());
        e.setAggregateType("classifier");
        e.setAggregateId("h_gender");
        e.setEventType("ClassifierUpdated");
        e.setPayload("{\"code\":\"h_gender\"}");
        e.setSchemaVersion(1);
        return e;
    }

    private SendResult<String, String> okSendResult(ProducerRecord<String, String> rec) {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(rec.topic(), 0), 0L, 0, 0L, 0, 0);
        return new SendResult<>(rec, metadata);
    }

    /** {@code send(ProducerRecord)} argument matcher — key bo'yicha. */
    private static ProducerRecord<String, String> recordWithKey(String key) {
        return ArgumentMatchers.argThat(rec -> rec != null && key.equals(rec.key()));
    }

    /** {@code send(ProducerRecord)} uchun "har qanday" matcher (raw-type warning'siz). */
    private static ProducerRecord<String, String> anyRecord() {
        return ArgumentMatchers.any();
    }

    private static ArgumentCaptor<ProducerRecord<String, String>> recordCaptor() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<ProducerRecord<String, String>> captor =
                (ArgumentCaptor) ArgumentCaptor.forClass(ProducerRecord.class);
        return captor;
    }

    private static String headerValue(ProducerRecord<String, String> rec, String name) {
        Header h = rec.headers().lastHeader(name);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("processBatch — happy path: topic/key/payload + hemis-event-id header, markPublished")
    void happyPath() {
        OutboxEvent event = buildEvent();
        when(repository.pollUnpublishedForUpdate(100)).thenReturn(List.of(event));

        // send(ProducerRecord) — 83d98de'dan beri poller header bilan jo'natadi
        when(kafkaTemplate.send(anyRecord()))
                .thenAnswer(inv -> {
                    ProducerRecord<String, String> rec = inv.getArgument(0);
                    return CompletableFuture.completedFuture(okSendResult(rec));
                });

        poller.processBatch();

        ArgumentCaptor<ProducerRecord<String, String>> captor = recordCaptor();
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, String> sent = captor.getValue();
        assertThat(sent.topic()).isEqualTo("hemis.classifier.events.v1");
        assertThat(sent.key()).isEqualTo("h_gender");
        assertThat(sent.value()).isEqualTo("{\"code\":\"h_gender\"}");
        // Idempotency kontrakti: outbox event.id header'da uzatiladi (Univer dedup buzilmasin)
        assertThat(headerValue(sent, EVENT_ID_HEADER)).isEqualTo(event.getId().toString());

        verify(repository).markPublished(event.getId());
        verify(repository, never()).markRetry(eq(event.getId()), anyString());
    }

    @Test
    @DisplayName("processBatch — empty batch, hech narsa qilmaydi")
    void emptyBatch_noOp() {
        when(repository.pollUnpublishedForUpdate(100)).thenReturn(List.of());

        poller.processBatch();

        verify(kafkaTemplate, never()).send(anyRecord());
        verify(repository, never()).markPublished(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("processBatch — Kafka exception → markRetry, published EMAS")
    void kafkaFailure_markRetry() {
        OutboxEvent event = buildEvent();
        when(repository.pollUnpublishedForUpdate(100)).thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("Kafka broker down"));
        when(kafkaTemplate.send(anyRecord())).thenReturn(failed);

        poller.processBatch();

        verify(repository, never()).markPublished(event.getId());
        // Xato sababi last_error'ga yozilishi kerak (NPE emas — haqiqiy Kafka xatosi)
        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).markRetry(eq(event.getId()), errorCaptor.capture());
        assertThat(errorCaptor.getValue()).contains("Kafka broker down");
    }

    @Test
    @DisplayName("processBatch — bir nechta event: muvaffaqiyatli va xato alohida")
    void mixedBatch_independentResults() {
        OutboxEvent ok = buildEvent();
        ok.setAggregateId("h_gender");
        OutboxEvent fail = buildEvent();
        fail.setAggregateId("h_university");

        when(repository.pollUnpublishedForUpdate(100)).thenReturn(List.of(ok, fail));

        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker"));

        when(kafkaTemplate.send(recordWithKey("h_gender")))
                .thenAnswer(inv -> {
                    ProducerRecord<String, String> rec = inv.getArgument(0);
                    return CompletableFuture.completedFuture(okSendResult(rec));
                });
        when(kafkaTemplate.send(recordWithKey("h_university")))
                .thenReturn(failed);

        poller.processBatch();

        verify(repository).markPublished(ok.getId());
        verify(repository).markRetry(eq(fail.getId()), anyString());
        verify(repository, never()).markPublished(fail.getId());
    }

    @Test
    @DisplayName("pollAndPublish — top-level exception scheduler'ni bloklab qo'ymaydi")
    void schedulerSurvivesException() {
        when(repository.pollUnpublishedForUpdate(anyInt()))
                .thenThrow(new RuntimeException("DB down"));

        // Should not throw
        poller.pollAndPublish();
    }

    @Test
    @DisplayName("cleanupOldPublished — deletePublishedBefore(now - 30d)")
    void cleanup_callsDelete() {
        when(repository.deletePublishedBefore(org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(42);

        poller.cleanupOldPublished();

        verify(repository).deletePublishedBefore(org.mockito.ArgumentMatchers.any(LocalDateTime.class));
    }

    @Test
    @DisplayName("cleanupOldPublished — 0 deleted → no log (no throw)")
    void cleanup_zeroDeleted() {
        when(repository.deletePublishedBefore(org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(0);

        poller.cleanupOldPublished();

        verify(repository).deletePublishedBefore(org.mockito.ArgumentMatchers.any(LocalDateTime.class));
    }
}
