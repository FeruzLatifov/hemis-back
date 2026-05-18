package uz.hemis.service.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Manual Kafka config — Spring Boot 4.0 da KafkaAutoConfiguration alohida modulga ko'chgan,
 * spring-boot-autoconfigure jar'iga kirmaydi. Shu sababli ProducerFactory + KafkaTemplate
 * + ListenerContainerFactory'ni manual deklaratsiya qilamiz.
 *
 * <p>ADR-0007 outbox + ADR-0012 webhook fanout + ADR-0010 employee sync inbound uchun.</p>
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class KafkaConfig {

    private static final int EMPLOYEE_SYNC_PARTITIONS = 12;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:hemis-back-default}")
    private String consumerGroupId;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Webhook dispatcher uchun RestClient.Builder.
     * Spring Boot 4.0 da auto-config split bo'lgan — manual bean.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Default container factory (concurrency=3) — webhook va boshqa konsumerlar.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        return factory;
    }

    // =====================================================
    // Topic auto-create (KafkaAdmin) — Univer sync inbound + DLQ
    // =====================================================

    /**
     * Topic auto-creator. Spring Boot 4.0 da KafkaAutoConfiguration split bo'lganidan,
     * KafkaAdmin manual e'lon qilinishi shart (NewTopic bean'lar bo'lishidan oldin).
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Univer → markaz xodim sync inbound topic. Partitions=12 (= consumer concurrency).
     * Key = PINFL → bir xil PINFL har doim bir xil partition'ga, demak bitta thread serial.
     */
    @Bean
    public NewTopic employeeSyncInboundTopic(
            @Value("${hemis.employee-sync.topics.inbound}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(EMPLOYEE_SYNC_PARTITIONS)
                .replicas(1)  // dev: 1 broker; prod K8s: 3 (Strimzi cluster)
                .build();
    }

    /**
     * Sync DLQ — retry tugagandan keyin ko'chiriladi, admin tomonidan replay.
     */
    @Bean
    public NewTopic employeeSyncDlqTopic(
            @Value("${hemis.employee-sync.topics.dlq}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(EMPLOYEE_SYNC_PARTITIONS)
                .replicas(1)
                .build();
    }

    // =====================================================
    // Employee sync error handler + container factory
    // =====================================================

    /**
     * DefaultErrorHandler: N marta retry → DLQ ga publish.
     * <p>FixedBackOff — INSERT ON CONFLICT idempotent, exponential kerak emas.</p>
     */
    @Bean
    public DefaultErrorHandler employeeSyncErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${hemis.employee-sync.topics.dlq}") String dlqTopic,
            @Value("${hemis.employee-sync.consumer.retry.max-attempts:3}") long maxAttempts,
            @Value("${hemis.employee-sync.consumer.retry.backoff-ms:1000}") long backoffMs) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(dlqTopic, record.partition()));
        // FixedBackOff(interval, maxAttempts) — maxAttempts = retry COUNT (initial call alohida)
        FixedBackOff backoff = new FixedBackOff(backoffMs, Math.max(0, maxAttempts - 1));
        return new DefaultErrorHandler(recoverer, backoff);
    }

    /**
     * Sync uchun alohida factory — concurrency yuqori (12 = partitions),
     * DLQ recoverer bilan. Default factory (webhook) tegmaydi.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> employeeSyncListenerContainerFactory(
            DefaultErrorHandler employeeSyncErrorHandler,
            @Value("${hemis.employee-sync.consumer.concurrency:12}") int concurrency) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        factory.setCommonErrorHandler(employeeSyncErrorHandler);
        return factory;
    }
}
