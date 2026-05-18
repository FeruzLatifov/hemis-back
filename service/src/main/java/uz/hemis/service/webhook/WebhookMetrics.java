package uz.hemis.service.webhook;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Webhook dispatcher Prometheus metrics.
 *
 * <p><strong>Metric'lar (Prometheus query'lari):</strong></p>
 * <pre>
 *   hemis_webhook_dispatch_total{status="success",university="337"}  - counter
 *   hemis_webhook_dispatch_total{status="failed",university="337"}   - counter
 *   hemis_webhook_dispatch_total{status="retry",university="337"}    - counter
 *   hemis_webhook_dispatch_total{status="dlq",university="337"}      - counter
 *   hemis_webhook_dispatch_duration_seconds{university="337"}        - timer (p50/p95/p99)
 * </pre>
 *
 * <p><strong>Grafana alert misol:</strong></p>
 * <pre>
 *   rate(hemis_webhook_dispatch_total{status="failed"}[5m]) > 0.1   - 6 fail/min
 *   histogram_quantile(0.95, hemis_webhook_dispatch_duration_seconds) > 5  - p95 >5s
 * </pre>
 *
 * @since ADR-0012
 */
@Component
public class WebhookMetrics {

    private static final String METRIC_DISPATCH = "hemis_webhook_dispatch";
    private static final String METRIC_DURATION = "hemis_webhook_dispatch_duration";
    private static final String TAG_UNIVERSITY = "university";
    private static final String TAG_STATUS = "status";

    private final MeterRegistry registry;

    public WebhookMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSuccess(String universityCode, Duration duration) {
        counter(universityCode, "success").increment();
        timer(universityCode).record(duration);
    }

    public void recordFailed(String universityCode, Duration duration) {
        counter(universityCode, "failed").increment();
        timer(universityCode).record(duration);
    }

    public void recordRetry(String universityCode) {
        counter(universityCode, "retry").increment();
    }

    public void recordDlq(String universityCode) {
        counter(universityCode, "dlq").increment();
    }

    private Counter counter(String universityCode, String status) {
        return Counter.builder(METRIC_DISPATCH + "_total")
                .tags(Tags.of(TAG_UNIVERSITY, universityCode, TAG_STATUS, status))
                .description("Webhook delivery attempt outcome counter")
                .register(registry);
    }

    private Timer timer(String universityCode) {
        return Timer.builder(METRIC_DURATION + "_seconds")
                .tag(TAG_UNIVERSITY, universityCode)
                .description("Webhook dispatch latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
