package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Test Legacy Service - Business logic for test/diagnostic endpoints
 *
 * <p>Extracted from TestServiceController to follow Clean Architecture.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TestLegacyService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Get students updated yesterday.
     *
     * <p>OLD-HEMIS: TestServiceBean.students()</p>
     * <p>JPQL: select e from hemishe_EStudent e where e.updateTs >= :yesterday and e.updateTs < :today</p>
     *
     * @return list of student rows or null if empty/error
     */
    public List<Map<String, Object>> getStudentsUpdatedYesterday() {
        String sql = """
                SELECT id, code, pinfl, serial_number, firstname, lastname, fathername,
                       "_university" as university, "_student_status" as student_status,
                       create_ts, update_ts
                FROM hemishe_e_student
                WHERE delete_ts IS NULL
                  AND update_ts >= CURRENT_DATE - 1
                  AND update_ts < CURRENT_DATE
                ORDER BY update_ts DESC
                """;

        List<Map<String, Object>> items = jdbcTemplate.queryForList(sql);

        if (items.isEmpty()) {
            return null;
        }
        return items;
    }
}
