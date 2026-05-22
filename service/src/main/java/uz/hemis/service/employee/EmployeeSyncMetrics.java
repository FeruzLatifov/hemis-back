package uz.hemis.service.employee;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Employee sync Prometheus metrics (ADR-0010).
 *
 * <p><strong>Metric'lar:</strong></p>
 * <pre>
 *   hemis_employee_sync_total{status="success",university="337"}   - counter
 *   hemis_employee_sync_total{status="failed",university="337"}    - counter
 *   hemis_employee_sync_total{status="skipped",university="337"}   - counter (duplicate/no-op)
 *   hemis_employee_sync_duration_seconds{university="337"}         - timer (p50/p95/p99)
 *   hemis_employee_sync_deserialize_failed_total                   - poison pill counter
 * </pre>
 *
 * <p><strong>Alert misollar:</strong></p>
 * <pre>
 *   rate(hemis_employee_sync_total{status="failed"}[5m]) > 0.5     - >30 fail/min
 *   rate(hemis_employee_sync_deserialize_failed_total[1h]) > 0     - poison pill
 *   histogram_quantile(0.95, hemis_employee_sync_duration_seconds) > 3
 * </pre>
 *
 * @since ADR-0010 (Employee sync outbox)
 */
@Component
public class EmployeeSyncMetrics {

    private static final String METRIC_SYNC = "hemis_employee_sync";
    private static final String METRIC_DURATION = "hemis_employee_sync_duration";
    private static final String METRIC_DESERIALIZE = "hemis_employee_sync_deserialize_failed";
    private static final String TAG_UNIVERSITY = "university";
    private static final String TAG_STATUS = "status";

    private final MeterRegistry registry;

    public EmployeeSyncMetrics(MeterRegistry registry) {
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

    public void recordSkipped(String universityCode) {
        counter(universityCode, "skipped").increment();
    }

    public void recordDeserializeFailure() {
        Counter.builder(METRIC_DESERIALIZE + "_total")
                .description("Poison pill — JSON deserialize failure on inbound sync topic")
                .register(registry)
                .increment();
    }

    private Counter counter(String universityCode, String status) {
        return Counter.builder(METRIC_SYNC + "_total")
                .tags(Tags.of(TAG_UNIVERSITY, universityCode, TAG_STATUS, status))
                .description("Employee sync attempt outcome counter")
                .register(registry);
    }

    private Timer timer(String universityCode) {
        return Timer.builder(METRIC_DURATION + "_seconds")
                .tag(TAG_UNIVERSITY, universityCode)
                .description("Employee sync end-to-end latency (consume → DB persist)")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
