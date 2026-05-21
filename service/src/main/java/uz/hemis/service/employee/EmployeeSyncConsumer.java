package uz.hemis.service.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import uz.hemis.common.dto.employee.EmployeeSyncEvent;

/**
 * Univer sync inbound consumer — Kafka topic'dan o'qib processor'ga uzatadi.
 *
 * <p>1 message = 1 transaction = 1 row.</p>
 *
 * <p>Concurrency = 12 (= topic partitions). Bir xil PINFL bir xil partition'ga
 * tushadi → bitta thread serial ishlaydi → optimistic-lock collision YO'Q.</p>
 *
 * <p>Xato ish boshqaruvi (KafkaConfig.employeeSyncErrorHandler):
 * <ul>
 *   <li>Throw → DefaultErrorHandler retry N marta (FixedBackOff)</li>
 *   <li>Hali xato → DLQ topic ga publish + offset commit</li>
 *   <li>Admin keyinchalik DLQ ni inspect / replay qiladi</li>
 * </ul></p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "hemis.employee-sync.enabled", havingValue = "true", matchIfMissing = true)
public class EmployeeSyncConsumer {

    private static final String DEFAULT_SYNC_USER = "univer-sync";

    private final EmployeeSyncProcessor processor;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${hemis.employee-sync.topics.inbound}",
            groupId = "${spring.kafka.consumer.group-id:hemis-back-default}-emp-sync",
            containerFactory = "employeeSyncListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
        EmployeeSyncEvent event;
        try {
            event = objectMapper.readValue(record.value(), EmployeeSyncEvent.class);
        } catch (Exception deserialErr) {
            // Poison pill — retry o'zi yordam bermaydi, lekin throw qilamiz →
            // DefaultErrorHandler max attempts'dan keyin DLQ'ga jo'natadi (offset commit ham qiladi).
            log.error("Cannot deserialize record partition={} offset={}: {}",
                    record.partition(), record.offset(), deserialErr.getMessage());
            io.sentry.Sentry.captureException(deserialErr, scope -> {
                scope.setLevel(io.sentry.SentryLevel.FATAL);
                scope.setTag("component", "employee_sync");
                scope.setTag("phase", "deserialize");
                scope.setExtra("kafka_partition", String.valueOf(record.partition()));
                scope.setExtra("kafka_offset", String.valueOf(record.offset()));
            });
            throw deserialErr;
        }

        String syncUser = (event.syncUser() != null && !event.syncUser().isBlank())
                ? event.syncUser() : DEFAULT_SYNC_USER;

        try {
            EmployeeSyncProcessor.ProcessResult result =
                    processor.process(event.universityCode(), event.payload(), syncUser);
            log.debug("Sync OK universityCode={} pinfl={} batchId={} empId={} jobId={}",
                    event.universityCode(),
                    mask(event.payload() != null ? event.payload().getPinfl() : null),
                    event.batchId(),
                    result.employeeId(),
                    result.jobId());
            ack.acknowledge();
        } catch (Exception processErr) {
            log.warn("Sync FAILED universityCode={} pinfl={} sourceUid={} batchId={}: {}",
                    event.universityCode(),
                    mask(event.payload() != null ? event.payload().getPinfl() : null),
                    event.payload() != null ? event.payload().getSourceUid() : null,
                    event.batchId(),
                    processErr.getMessage());
            final EmployeeSyncEvent capturedEvent = event;
            io.sentry.Sentry.captureException(processErr, scope -> {
                scope.setLevel(io.sentry.SentryLevel.WARNING);
                scope.setTag("component", "employee_sync");
                scope.setTag("phase", "process");
                scope.setTag("university_code", capturedEvent.universityCode());
                scope.setExtra("batch_id", String.valueOf(capturedEvent.batchId()));
                scope.setExtra("source_uid",
                        capturedEvent.payload() != null ? capturedEvent.payload().getSourceUid() : null);
                // PINFL TAQIQ — log'da mask qilingan, Sentry'ga ham yubormaslik
            });
            throw processErr;
        }
    }

    private static String mask(String pinfl) {
        if (pinfl == null || pinfl.length() < 6) return "***";
        return pinfl.substring(0, 4) + "****" + pinfl.substring(pinfl.length() - 2);
    }
}
