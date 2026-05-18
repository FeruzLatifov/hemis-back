package uz.hemis.service.employee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.employee.EmployeeSyncDto;
import uz.hemis.common.dto.employee.EmployeeSyncEvent;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * KafkaTemplate wrapper — Univer sync HTTP entry → Kafka topic.
 *
 * <p>Key = PINFL → partition routing kafolati (bir xil PINFL = bir xil partition =
 * bir consumer thread serial). Demak optimistic-lock collision yo'qoladi.</p>
 *
 * <p>Send semantikasi: {@code acks=all} (KafkaConfig'da idempotent producer).
 * {@link CompletableFuture} qaytariladi — controller batch barcha futures'ni
 * {@code allOf().get(timeout)} bilan kutishi kerak (HTTP 202'dan oldin).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeSyncProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hemis.employee-sync.topics.inbound}")
    private String topic;

    public CompletableFuture<?> publish(UUID batchId,
                                        String universityCode,
                                        String syncUser,
                                        EmployeeSyncDto dto) {
        EmployeeSyncEvent event = new EmployeeSyncEvent(
                batchId, universityCode, syncUser, dto, Instant.now());

        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize sync event: batchId={} sourceUid={}",
                    batchId, dto.getSourceUid(), e);
            return CompletableFuture.failedFuture(e);
        }

        return kafkaTemplate.send(topic, dto.getPinfl(), json);
    }
}
