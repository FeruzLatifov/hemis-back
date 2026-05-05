package uz.hemis.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Citizenship classifier validator — caches active-code lookups (24h TTL).
 *
 * <p><strong>Maqsad:</strong> {@code StudentEnrollmentService.generateStudentId} har talaba
 * yaratishda citizenship code'ni {@code citizenship} jadvalidan tekshirardi (1 query/enrollment).
 * Talaba ro'yxatga olish hot-path — minglab so'rov/soat. ~250 ta davlat (closed list)
 * 24h cache bilan boot vaqtda yuklanadi.</p>
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
public class CitizenshipValidator {

    private final JdbcTemplate jdbcTemplate;

    /**
     * @param code citizenship classifier code (e.g. "UZB")
     * @return {@code true} if active, {@code false} if not found / inactive.
     */
    @Cacheable(value = "citizenshipActive", key = "#code", unless = "#result == null")
    public Boolean isActive(String code) {
        if (code == null || code.isBlank()) return Boolean.FALSE;
        try {
            jdbcTemplate.queryForMap(
                    "SELECT code FROM citizenship WHERE code = ? AND is_active = true",
                    code);
            return Boolean.TRUE;
        } catch (EmptyResultDataAccessException e) {
            return Boolean.FALSE;
        }
    }
}
