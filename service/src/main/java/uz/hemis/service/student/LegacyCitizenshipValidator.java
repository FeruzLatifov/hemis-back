package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Legacy citizenship classifier validator — eski CUBA {@code hemishe_h_citizenship}
 * jadvaliga ulashadi (24h TTL cache).
 *
 * <p><strong>Maqsad:</strong> {@code StudentEnrollmentService.generateStudentId} (api-legacy
 * orqali Univer chaqirgan endpoint) har talaba yaratishda citizenship code'ni tekshiradi.
 * Talaba ro'yxatga olish hot-path — minglab so'rov/soat. ~250 ta davlat (closed list)
 * 24h cache bilan yuklanadi.</p>
 *
 * <p><strong>Naming convention</strong> ({@code api-legacy/CLAUDE.md} GOLDEN RULE):
 * {@code Legacy*} prefiks — bu komponent faqat eski {@code hemishe_*} jadvallarga ulanadi.
 * Modern schema (yangi {@code citizenship} jadval, hozircha mavjud emas) uchun alohida
 * {@code CitizenshipValidator} (prefiks-siz) kerak bo'lganda yaratiladi.</p>
 *
 * <p><strong>Pattern:</strong> Alohida bean — AOP self-invocation tuzog'idan saqlanish
 * (same-class chaqiruv {@code @Cacheable} ni bypass qiladi).</p>
 *
 * @since 2.5.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LegacyCitizenshipValidator {

    private final JdbcTemplate jdbcTemplate;

    /**
     * @param code citizenship classifier code (e.g. "UZB")
     * @return {@code true} if active, {@code false} if not found / inactive.
     */
    @Cacheable(value = "citizenshipActive", key = "#code", unless = "#result == null")
    public Boolean isActive(String code) {
        if (code == null || code.isBlank()) return Boolean.FALSE;
        try {
            // Old-hemis 1:1 — eski CUBA jadval (hemishe_h_citizenship). Yangi `citizenship`
            // jadval bizda yo'q (api-legacy/CLAUDE.md GOLDEN RULE: api-legacy = eski jadvallar).
            jdbcTemplate.queryForMap(
                    "SELECT code FROM hemishe_h_citizenship WHERE code = ? AND delete_ts IS NULL",
                    code);
            return Boolean.TRUE;
        } catch (EmptyResultDataAccessException e) {
            return Boolean.FALSE;
        }
    }
}
