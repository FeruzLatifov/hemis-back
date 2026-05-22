package uz.hemis.service.outbox;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Outbox poller Prometheus metrics (ADR-0007).
 *
 * <p><strong>Metric'lar:</strong></p>
 * <pre>
 *   hemis_outbox_publish_total{status="success",topic="hemis.employee.events.v1"}  - counter
 *   hemis_outbox_publish_total{status="failed",topic="hemis.classifier.events.v1"} - counter
 *   hemis_outbox_publish_duration_seconds                                          - timer
 *   hemis_outbox_queue_depth                                                       - gauge (pending events)
 *   hemis_outbox_retention_deleted_total                                           - counter (daily cleanup)
 * </pre>
 *
 * <p><strong>Alert misollar:</strong></p>
 * <pre>
 *   hemis_outbox_queue_depth > 1000               - publish stuck, broker down?
 *   rate(hemis_outbox_publish_total{status="failed"}[5m]) > 1
 * </pre>
 *
 * @since ADR-0007 (Kafka-first sync)
 */
@Component
public class OutboxMetrics {

    private static final String METRIC_PUBLISH = "hemis_outbox_publish";
    private static final String METRIC_DURATION = "hemis_outbox_publish_duration";
    private static final String METRIC_QUEUE_DEPTH = "hemis_outbox_queue_depth";
    private static final String METRIC_RETENTION = "hemis_outbox_retention_deleted";
    private static final String TAG_TOPIC = "topic";
    private static final String TAG_STATUS = "status";

    private final MeterRegistry registry;
    private final AtomicLong queueDepth = new AtomicLong(0);

    public OutboxMetrics(MeterRegistry registry) {
        this.registry = registry;
        Gauge.builder(METRIC_QUEUE_DEPTH, queueDepth, AtomicLong::doubleValue)
                .description("Outbox pending event count (published_at IS NULL)")
                .register(registry);
    }

    public void recordSuccess(String topic, Duration duration) {
        counter(topic, "success").increment();
        timer().record(duration);
    }

    public void recordFailed(String topic) {
        counter(topic, "failed").increment();
    }

    public void updateQueueDepth(long pendingCount) {
        queueDepth.set(pendingCount);
    }

    public void recordRetentionDeleted(int rowsDeleted) {
        Counter.builder(METRIC_RETENTION + "_total")
                .description("Outbox retention daily cleanup — deleted rows count")
                .register(registry)
                .increment(rowsDeleted);
    }

    private Counter counter(String topic, String status) {
        return Counter.builder(METRIC_PUBLISH + "_total")
                .tags(Tags.of(TAG_TOPIC, topic, TAG_STATUS, status))
                .description("Outbox poller publish outcome counter")
                .register(registry);
    }

    private Timer timer() {
        return Timer.builder(METRIC_DURATION + "_seconds")
                .description("Outbox event publish latency (DB read → Kafka ack)")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
