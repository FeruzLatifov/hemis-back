package uz.hemis.service.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import uz.hemis.common.dto.employee.EmployeeSyncDto;
import uz.hemis.common.dto.employee.EmployeeSyncEvent;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeSyncConsumer — Kafka inbound + DLQ retry semantik")
class EmployeeSyncConsumerTest {

    @Mock private EmployeeSyncProcessor processor;
    @Mock private Acknowledgment ack;

    private EmployeeSyncConsumer consumer;
    private ObjectMapper realObjectMapper;

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        consumer = new EmployeeSyncConsumer(processor, realObjectMapper);
    }

    @Test
    @DisplayName("happy path — processor.process() chaqiriladi, ack qilinadi")
    void happyPath() throws Exception {
        EmployeeSyncDto payload = EmployeeSyncDto.builder()
                .pinfl("12345678901234")
                .sourceUid("UNIVER-1")
                .build();
        EmployeeSyncEvent event = new EmployeeSyncEvent(
                UUID.randomUUID(), "337", "admin@univer", payload, Instant.now());

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "hemis.employee.sync.inbound.v1", 0, 0L, "12345678901234",
                realObjectMapper.writeValueAsString(event));

        when(processor.process("337", payload, "admin@univer"))
                .thenReturn(new EmployeeSyncProcessor.ProcessResult(UUID.randomUUID(), UUID.randomUUID()));

        consumer.consume(record, ack);

        verify(processor).process("337", payload, "admin@univer");
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("syncUser null → DEFAULT_SYNC_USER (univer-sync)")
    void nullSyncUser_usesDefault() throws Exception {
        EmployeeSyncDto payload = EmployeeSyncDto.builder()
                .pinfl("12345678901234").build();
        EmployeeSyncEvent event = new EmployeeSyncEvent(
                null, "337", null, payload, Instant.now());

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, "pinfl",
                realObjectMapper.writeValueAsString(event));

        when(processor.process("337", payload, "univer-sync"))
                .thenReturn(new EmployeeSyncProcessor.ProcessResult(UUID.randomUUID(), UUID.randomUUID()));

        consumer.consume(record, ack);

        verify(processor).process("337", payload, "univer-sync");
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("invalid JSON — DeserializationException throw, ack qilinmaydi (DLQ retry)")
    void invalidJson_throwsForDlq() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, "k", "INVALID-JSON-{");

        assertThatThrownBy(() -> consumer.consume(record, ack))
                .isInstanceOf(Exception.class);

        verify(ack, never()).acknowledge();
        verify(processor, never()).process(anyString(), org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    @DisplayName("processor.process throws — exception bubbled, ack qilinmaydi (retry/DLQ)")
    void processFails_noAck() throws Exception {
        EmployeeSyncDto payload = EmployeeSyncDto.builder()
                .pinfl("12345678901234").build();
        EmployeeSyncEvent event = new EmployeeSyncEvent(
                UUID.randomUUID(), "337", "admin", payload, Instant.now());

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, "k",
                realObjectMapper.writeValueAsString(event));

        when(processor.process("337", payload, "admin"))
                .thenThrow(new RuntimeException("DB constraint violation"));

        assertThatThrownBy(() -> consumer.consume(record, ack))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB constraint");

        verify(ack, never()).acknowledge();
    }

    @Test
    @DisplayName("blank syncUser ham DEFAULT'ga fallback")
    void blankSyncUser_usesDefault() throws Exception {
        EmployeeSyncDto payload = EmployeeSyncDto.builder()
                .pinfl("12345678901234").build();
        EmployeeSyncEvent event = new EmployeeSyncEvent(
                null, "337", "   ", payload, Instant.now());

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, "k",
                realObjectMapper.writeValueAsString(event));

        when(processor.process("337", payload, "univer-sync"))
                .thenReturn(new EmployeeSyncProcessor.ProcessResult(UUID.randomUUID(), UUID.randomUUID()));

        consumer.consume(record, ack);

        verify(processor).process("337", payload, "univer-sync");
    }
}
