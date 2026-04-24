package uz.hemis.service.infrastructure;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

/**
 * Building modul uchun custom Micrometer metrics.
 * Prometheus/Grafana'ga eksport bo'ladi (/actuator/prometheus).
 *
 * <p>Kuzatuv:
 * <ul>
 *   <li>Auto-fill effectiveness (cadastre hit rate)</li>
 *   <li>Sync natijasi (per OTM, per status)</li>
 *   <li>Creation source distribution (univer/manual/excel)</li>
 * </ul></p>
 */
@Component
public class BuildingMetrics {

    private final MeterRegistry registry;
    private final Counter autofillHits;
    private final Counter autofillMisses;

    public BuildingMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.autofillHits = Counter.builder("buildings.autofill.cadastre.hit")
                .description("cad_number via cadastre auto-fill topildi")
                .register(registry);
        this.autofillMisses = Counter.builder("buildings.autofill.cadastre.miss")
                .description("cad_number berilgan, lekin cadastre'da topilmadi")
                .register(registry);
    }

    public void recordAutofillHit() {
        autofillHits.increment();
    }

    public void recordAutofillMiss() {
        autofillMisses.increment();
    }

    /** Univer sync natija — OTM kodi va status bilan taglanadi. */
    public void recordSyncOutcome(String universityCode, String status) {
        Counter.builder("buildings.sync.count")
                .description("Univer tomondan sync natijasi")
                .tags(Tags.of("university", universityCode, "status", status))
                .register(registry)
                .increment();
    }

    /** Bino yaratish — source (univer/manual/excel) bo'yicha. */
    public void recordBuildingCreated(String source) {
        Counter.builder("buildings.created.total")
                .description("Yaratilgan binolar soni (source bo'yicha)")
                .tag("source", source)
                .register(registry)
                .increment();
    }

    /** Lifecycle event yozildi — event turi bilan. */
    public void recordLifecycleEvent(String eventType) {
        Counter.builder("buildings.lifecycle.events.total")
                .description("Bino lifecycle voqealar soni (type bo'yicha)")
                .tag("type", eventType)
                .register(registry)
                .increment();
    }
}
