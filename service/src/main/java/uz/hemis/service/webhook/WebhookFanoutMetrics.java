package uz.hemis.service.webhook;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

/**
 * Webhook fanout consumer Prometheus metrics (ADR-0012).
 *
 * <p>Domain Kafka topic (classifier.events.v1, rule.events.v1, …) → per-OTM
 * webhook.events fanout. Bu metric'lar fanout consumer'ning sog'lig'ini
 * kuzatadi (Dispatcher metric'lariga qo'shimcha).</p>
 *
 * <pre>
 *   hemis_webhook_fanout_total{topic="hemis.classifier.events.v1",status="success"} - counter
 *   hemis_webhook_fanout_total{topic="hemis.rule.events.v1",status="failed"}        - counter
 *   hemis_webhook_fanout_targets{topic="hemis.classifier.events.v1"}                - counter (224 OTM uchun fan-out hisobi)
 * </pre>
 *
 * @since ADR-0012 (Webhook outbound infrastructure)
 */
@Component
public class WebhookFanoutMetrics {

    private static final String METRIC_FANOUT = "hemis_webhook_fanout";
    private static final String METRIC_TARGETS = "hemis_webhook_fanout_targets";
    private static final String TAG_TOPIC = "topic";
    private static final String TAG_STATUS = "status";

    private final MeterRegistry registry;

    public WebhookFanoutMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSuccess(String topic, int targetCount) {
        counter(topic, "success").increment();
        Counter.builder(METRIC_TARGETS + "_total")
                .tag(TAG_TOPIC, topic)
                .description("Webhook fanout — per-OTM target message count")
                .register(registry)
                .increment(targetCount);
    }

    public void recordFailed(String topic) {
        counter(topic, "failed").increment();
    }

    private Counter counter(String topic, String status) {
        return Counter.builder(METRIC_FANOUT + "_total")
                .tags(Tags.of(TAG_TOPIC, topic, TAG_STATUS, status))
                .description("Webhook fanout consumer outcome counter")
                .register(registry);
    }
}
