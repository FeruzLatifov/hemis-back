package uz.hemis.service.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kadastr PENDING retry — API o'lik bo'lganda {@code fetch_status=PENDING} qolgan obyektlarni
 * davriy qayta oladi (API tiklanганда COMPLETE bo'ladi). Bino saqlash bloklanmaydi.
 *
 * <p>ingestByCadNum ALOHIDA bean chaqiruvi (proxy per-item {@code @Transactional} qo'llaydi) —
 * biri fail bo'lsa qolgani ta'sirlanmaydi.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CadastreRetryScheduler {

    private final CadastreIngestService ingestService;

    @Scheduled(cron = "${hemis.cadastre.retry.cron:0 */15 * * * *}")   // har 15 daqiqa
    public void retryPending() {
        List<String> pending = ingestService.findPendingCadNumbers(50);
        if (pending.isEmpty()) {
            return;
        }
        int completed = 0, still = 0;
        for (String cad : pending) {
            try {
                var c = ingestService.ingestByCadNum(cad);
                if ("COMPLETE".equals(c.getFetchStatus())) completed++; else still++;
            } catch (Exception e) {
                still++;
                log.warn("Cadastre retry failed: cad={}, error={}", cad, e.getMessage());
            }
        }
        log.info("Cadastre retry done: pending={}, completed={}, still={}", pending.size(), completed, still);
    }
}
