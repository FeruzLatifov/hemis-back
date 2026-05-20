package uz.hemis.service.outbox;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import uz.hemis.domain.entity.outbox.OutboxEvent;
import uz.hemis.domain.repository.outbox.OutboxEventRepository;

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

    @SuppressWarnings("unchecked")
    private SendResult<String, String> okSendResult(String topic) {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(topic, 0), 0L, 0, 0L, 0, 0);
        ProducerRecord<String, String> rec = new ProducerRecord<>(topic, "k", "v");
        return new SendResult<>(rec, metadata);
    }

    @Test
    @DisplayName("processBatch — happy path: publish + markPublished")
    void happyPath() {
        OutboxEvent event = buildEvent();
        when(repository.pollUnpublishedForUpdate(100)).thenReturn(List.of(event));

        String expectedTopic = "hemis.classifier.events.v1";
        when(kafkaTemplate.send(eq(expectedTopic), eq("h_gender"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(okSendResult(expectedTopic)));

        poller.processBatch();

        verify(kafkaTemplate).send(expectedTopic, "h_gender", "{\"code\":\"h_gender\"}");
        verify(repository).markPublished(event.getId());
        verify(repository, never()).markRetry(eq(event.getId()), anyString());
    }

    @Test
    @DisplayName("processBatch — empty batch, hech narsa qilmaydi")
    void emptyBatch_noOp() {
        when(repository.pollUnpublishedForUpdate(100)).thenReturn(List.of());

        poller.processBatch();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
        verify(repository, never()).markPublished(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("processBatch — Kafka exception → markRetry, published EMAS")
    void kafkaFailure_markRetry() {
        OutboxEvent event = buildEvent();
        when(repository.pollUnpublishedForUpdate(100)).thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("Kafka broker down"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(failed);

        poller.processBatch();

        verify(repository, never()).markPublished(event.getId());
        verify(repository).markRetry(eq(event.getId()), anyString());
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

        when(kafkaTemplate.send(anyString(), eq("h_gender"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(okSendResult("hemis.classifier.events.v1")));
        when(kafkaTemplate.send(anyString(), eq("h_university"), anyString()))
                .thenReturn(failed);

        poller.processBatch();

        verify(repository).markPublished(ok.getId());
        verify(repository).markRetry(eq(fail.getId()), anyString());
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
