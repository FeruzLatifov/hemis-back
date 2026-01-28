package uz.hemis.service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import uz.hemis.service.dashboard.DashboardService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardCacheWarmup {

    private final DashboardService dashboardService;

    // Warm up cache shortly after application starts (does not block startup)
    @EventListener(ApplicationReadyEvent.class)
    public void warmupOnStartup() {
        new Thread(() -> {
            try {
                log.info("🔥 Warming up dashboard cache on startup...");
                dashboardService.getDashboardStats();
                log.info("✅ Dashboard cache warmup completed");
            } catch (Exception e) {
                log.warn("⚠️ Dashboard cache warmup failed: {}", e.toString());
            }
        }, "dashboard-cache-warmup").start();
    }

    // Refresh cache every 30 minutes proactively
    @Scheduled(fixedRate = 30 * 60 * 1000L, initialDelay = 5 * 60 * 1000L)
    public void refreshCachePeriodically() {
        try {
            log.info("♻️ Refreshing dashboard cache (scheduled)...");
            dashboardService.getDashboardStats();
            log.info("✅ Dashboard cache refresh done");
        } catch (Exception e) {
            log.warn("⚠️ Dashboard cache refresh failed: {}", e.toString());
        }
    }
}
