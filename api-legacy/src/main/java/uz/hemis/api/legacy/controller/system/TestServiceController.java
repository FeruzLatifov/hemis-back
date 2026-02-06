package uz.hemis.api.legacy.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Test Service Controller - CUBA REST API Compatible
 *
 * <p>OLD-HEMIS: TestServiceBean (healthcheck, students)</p>
 * <p>healthcheck - anonymousAllowed (no auth required)</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/test")
@Tag(name = "99.Test", description = "Test xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class TestServiceController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Health check (anonymous - no auth required)
     *
     * <p>OLD-HEMIS: TestServiceBean.healthcheck()</p>
     * <p>Returns: {"status": "ok"}</p>
     */
    @GetMapping("/healthcheck")
    @Operation(summary = "Health check (anonymous)", description = "Tizim holatini tekshirish. Autentifikatsiya talab qilinmaydi.")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> healthcheck() {
        log.info("[CUBA Service] test/healthcheck");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        return ResponseEntity.ok(result);
    }

    /**
     * Get students updated yesterday
     *
     * <p>OLD-HEMIS: TestServiceBean.students()</p>
     * <p>JPQL: select e from hemishe_EStudent e where e.updateTs >= :yesterday and e.updateTs < :today</p>
     */
    @GetMapping("/students")
    @Operation(summary = "Kechagi yangilangan talabalar", description = "Kecha yangilangan talabalar ro'yxati")
    public ResponseEntity<?> students() {
        log.info("[CUBA Service] test/students");

        try {
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
                return ResponseEntity.ok(null);
            }
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error fetching yesterday's students", e);
            return ResponseEntity.ok(null);
        }
    }
}
