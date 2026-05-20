package uz.hemis.service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import uz.hemis.domain.entity.outbox.OutboxEvent;
import uz.hemis.domain.repository.outbox.OutboxEventRepository;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link OutboxEventPublisher} unit testlar — outbox pattern semantik.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventPublisher")
class OutboxEventPublisherTest {

    @Mock private OutboxEventRepository repository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxEventPublisher publisher;

    @Nested
    @DisplayName("publish(simple)")
    class SimplePublish {

        @Test
        @DisplayName("happy path — event saqlanadi, ID qaytariladi")
        void happyPath_savesAndReturnsId() throws Exception {
            UUID generatedId = UUID.randomUUID();
            OutboxEvent saved = new OutboxEvent();
            saved.setId(generatedId);

            when(objectMapper.writeValueAsString(any())).thenReturn("{\"x\":1}");
            when(repository.save(any(OutboxEvent.class))).thenReturn(saved);

            UUID id = publisher.publish("classifier", "abc-123", "created",
                    Map.of("x", 1));

            assertThat(id).isEqualTo(generatedId);
            verify(repository).save(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("event field'lar to'g'ri to'ldiriladi")
        void eventFields_populatedCorrectly() throws Exception {
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"val\"}");
            when(repository.save(any(OutboxEvent.class))).thenAnswer(inv -> {
                OutboxEvent e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            publisher.publish("employee", "emp-456", "updated", Map.of("key", "val"));

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(repository).save(captor.capture());

            OutboxEvent event = captor.getValue();
            assertThat(event.getAggregateType()).isEqualTo("employee");
            assertThat(event.getAggregateId()).isEqualTo("emp-456");
            assertThat(event.getEventType()).isEqualTo("updated");
            assertThat(event.getSchemaVersion()).isEqualTo(1);
            assertThat(event.getPayload()).isEqualTo("{\"key\":\"val\"}");
            assertThat(event.getOccurredAt()).isNotNull();
            assertThat(event.getCreatedBy()).isNotNull(); // "system" yoki Authentication name
        }

        @Test
        @DisplayName("Jackson JsonProcessingException → OutboxPublishException re-throw")
        void serializationFailure_wrappedException() throws Exception {
            when(objectMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("Cannot serialize") {});

            assertThatThrownBy(() -> publisher.publish("classifier", "x", "created", Map.of()))
                    .isInstanceOf(OutboxPublishException.class)
                    .hasMessageContaining("Failed to serialize")
                    .hasMessageContaining("classifier/x");
        }
    }

    @Nested
    @DisplayName("publish(with correlation/causation)")
    class CorrelationTracking {

        @Test
        @DisplayName("explicit correlationId/causationId — eventga yoziladi")
        void explicitCorrelation_storedInEvent() throws Exception {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
            when(repository.save(any())).thenAnswer(inv -> {
                OutboxEvent e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            publisher.publish("classifier", "x", "created", Map.of(),
                    "corr-abc", "caus-def");

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getCorrelationId()).isEqualTo("corr-abc");
            assertThat(captor.getValue().getCausationId()).isEqualTo("caus-def");
        }

        @Test
        @DisplayName("MDC correlationId — avtomatik picked up (simple publish)")
        void mdcCorrelation_pickedUp() throws Exception {
            MDC.put("correlationId", "mdc-trace-123");
            try {
                when(objectMapper.writeValueAsString(any())).thenReturn("{}");
                when(repository.save(any())).thenAnswer(inv -> {
                    OutboxEvent e = inv.getArgument(0);
                    e.setId(UUID.randomUUID());
                    return e;
                });

                publisher.publish("classifier", "x", "created", Map.of());

                ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
                verify(repository).save(captor.capture());
                assertThat(captor.getValue().getCorrelationId()).isEqualTo("mdc-trace-123");
            } finally {
                MDC.remove("correlationId");
            }
        }

        @Test
        @DisplayName("MDC traceId fallback (Micrometer) — correlationId yo'q bo'lsa")
        void mdcTraceIdFallback() throws Exception {
            MDC.put("traceId", "micrometer-trace-456");
            try {
                when(objectMapper.writeValueAsString(any())).thenReturn("{}");
                when(repository.save(any())).thenAnswer(inv -> {
                    OutboxEvent e = inv.getArgument(0);
                    e.setId(UUID.randomUUID());
                    return e;
                });

                publisher.publish("classifier", "x", "created", Map.of());

                ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
                verify(repository).save(captor.capture());
                assertThat(captor.getValue().getCorrelationId()).isEqualTo("micrometer-trace-456");
            } finally {
                MDC.remove("traceId");
            }
        }

        @Test
        @DisplayName("MDC bo'sh — correlationId null")
        void noMdc_correlationIdNull() throws Exception {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
            when(repository.save(any())).thenAnswer(inv -> {
                OutboxEvent e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            publisher.publish("classifier", "x", "created", Map.of());

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getCorrelationId()).isNull();
        }
    }
}
