package uz.hemis.service.outbox;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

/**
 * Kafka topic auto-creation Bean.
 *
 * <p>Spring boot ishga tushganda KafkaAdmin bu Bean'lardan topic ro'yxatini oladi va
 * agar topic mavjud bo'lmasa, yaratadi. Production'da admin tomonidan oldindan
 * yaratilgan topic'lar (proper partitioning, retention) ham qabul qilinadi.</p>
 *
 * <p><strong>Topic naming convention (ADR-0007):</strong></p>
 * <pre>
 *   hemis.{aggregateType}.events.v{schemaVersion}
 *
 *   hemis.classifier.events.v1   — markaz h_* update'lari
 *   hemis.rule.events.v1         — talaba lock, baho lock qoidalar
 *   hemis.employee.events.v1     — Employee CRUD (ADR-0010)
 *   hemis.webhook.events         — webhook fanout (Univer'ga REST callback)
 *   hemis.webhook.dlq            — failed delivery (manual review)
 * </pre>
 *
 * @see <a href="https://docs.spring.io/spring-kafka/reference/kafka/configuring-topics.html">Spring Kafka topic config</a>
 * @since ADR-0007 Stage 1
 */
@Configuration
@ConditionalOnProperty(name = "hemis.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopicConfig {

    /** Default partition count (dev). Production K8s + Strimzi: 6 partition. */
    private static final int DEFAULT_PARTITIONS = 3;

    /** Dev replication factor=1 (single broker). Production: 3. */
    private static final short DEFAULT_REPLICAS = 1;

    /** Retention 30 kun (ms). Compliance audit uchun. */
    private static final long RETENTION_30_DAYS_MS = 30L * 24 * 3600 * 1000;

    /** DLQ retention 90 kun — manual triage uchun ko'proq vaqt. */
    private static final long RETENTION_90_DAYS_MS = 90L * 24 * 3600 * 1000;

    @Bean
    public KafkaAdmin.NewTopics outboxTopics() {
        return new KafkaAdmin.NewTopics(
                // Domain event topics — internal subscribers (audit, analytics, DB sync)
                topic("hemis.classifier.events.v1", DEFAULT_PARTITIONS, RETENTION_30_DAYS_MS),
                topic("hemis.rule.events.v1", DEFAULT_PARTITIONS, RETENTION_30_DAYS_MS),
                topic("hemis.employee.events.v1", DEFAULT_PARTITIONS, RETENTION_30_DAYS_MS),
                topic("hemis.student.events.v1", DEFAULT_PARTITIONS, RETENTION_30_DAYS_MS),
                topic("hemis.university.events.v1", DEFAULT_PARTITIONS, RETENTION_30_DAYS_MS),

                // Webhook fanout — markaz → 224 Univer (ADR-0012)
                topic("hemis.webhook.events", 6, RETENTION_30_DAYS_MS),

                // Dead Letter Queue — failed delivery (admin manual triage)
                topic("hemis.webhook.dlq", DEFAULT_PARTITIONS, RETENTION_90_DAYS_MS)
        );
    }

    private NewTopic topic(String name, int partitions, long retentionMs) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(DEFAULT_REPLICAS)
                .configs(Map.of(
                        "retention.ms", String.valueOf(retentionMs),
                        "compression.type", "snappy",
                        "min.insync.replicas", "1"  // Dev=1, prod=2 (Strimzi override)
                ))
                .build();
    }
}
