package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cached statistics about Student table — large-table estimates (no full COUNT).
 *
 * <p><strong>Maqsad:</strong> Health check va dashboard'lar uchun {@code SELECT COUNT(*)
 * FROM hemishe_e_student} 1.15M qator bo'yicha 5+ sekund — har request uchun
 * sequential scan. Endi {@code pg_class.reltuples} planner statistikasi (autovacuum
 * yangilab turadi) ishlatiladi: ~1 ms.</p>
 *
 * <p><strong>Pattern:</strong> Alohida bean — AOP self-invocation tuzog'idan
 * saqlanish ({@code @Cacheable} same-class call'larni bypass qiladi).</p>
 *
 * @since 2.5.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentStatsCache {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Total student row estimate (active + soft-deleted).
     *
     * <p>Source: {@code pg_class.reltuples} — planner statistic. Updated by autovacuum
     * (default ~10% rows changed). Off by &lt; 5% in steady state.</p>
     *
     * <p>Fallback: 0 if pg_class entry missing (table not yet analyzed) or query fails.</p>
     */
    @Cacheable(value = "studentCountEstimate", key = "'all'")
    public long estimateTotalCount() {
        try {
            Long est = jdbcTemplate.queryForObject(
                    "SELECT GREATEST(reltuples::bigint, 0) FROM pg_class WHERE relname = 'hemishe_e_student'",
                    Long.class);
            return est != null ? est : 0L;
        } catch (Exception e) {
            log.warn("Failed to read pg_class estimate for student count: {}", e.getMessage());
            return 0L;
        }
    }
}
