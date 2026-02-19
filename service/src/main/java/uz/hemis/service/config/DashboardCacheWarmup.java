package uz.hemis.service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import uz.hemis.service.dashboard.DashboardService;
import uz.hemis.service.student.StudentWebService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardCacheWarmup {

    private final DashboardService dashboardService;
    private final StudentWebService studentWebService;

    // Warm up cache shortly after application starts (does not block startup)
    @EventListener(ApplicationReadyEvent.class)
    public void warmupOnStartup() {
        new Thread(() -> {
            try {
                log.info("Warming up dashboard cache on startup...");
                dashboardService.getDashboardStats();
                log.info("Dashboard cache warmup completed");
            } catch (Exception e) {
                log.warn("Dashboard cache warmup failed: {}", e.toString());
            }

            // Directions page: summary + first page (no filters)
            try {
                log.info("Warming up directions cache on startup...");
                studentWebService.getSpecialitySummary();
                studentWebService.getSpecialityStats(null, null, null, PageRequest.of(0, 20));
                log.info("Directions cache warmup completed");
            } catch (Exception e) {
                log.warn("Directions cache warmup failed: {}", e.toString());
            }
        }, "dashboard-cache-warmup").start();
    }

    // Refresh cache every 30 minutes proactively
    @Scheduled(fixedRate = 30 * 60 * 1000L, initialDelay = 5 * 60 * 1000L)
    public void refreshCachePeriodically() {
        try {
            log.info("Refreshing dashboard cache (scheduled)...");
            dashboardService.getDashboardStats();
            log.info("Dashboard cache refresh done");
        } catch (Exception e) {
            log.warn("Dashboard cache refresh failed: {}", e.toString());
        }

        try {
            studentWebService.getSpecialitySummary();
            studentWebService.getSpecialityStats(null, null, null, PageRequest.of(0, 20));
        } catch (Exception e) {
            log.warn("Directions cache refresh failed: {}", e.toString());
        }
    }

    // Refresh duplicate materialized view every hour (non-blocking)
    @Scheduled(fixedRate = 60 * 60 * 1000L, initialDelay = 10 * 60 * 1000L)
    public void refreshDuplicateMaterializedView() {
        try {
            log.info("♻️ Refreshing mv_student_duplicates (scheduled)...");
            studentWebService.refreshDuplicateMaterializedView();
            log.info("✅ mv_student_duplicates refresh done");
        } catch (Exception e) {
            log.warn("⚠️ mv_student_duplicates refresh failed: {}", e.toString());
        }
    }
}
