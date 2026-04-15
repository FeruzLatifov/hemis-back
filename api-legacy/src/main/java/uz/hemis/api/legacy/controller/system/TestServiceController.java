package uz.hemis.api.legacy.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.legacy.TestLegacyService;

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

    private final TestLegacyService testLegacyService;

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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Kechagi yangilangan talabalar", description = "Kecha yangilangan talabalar ro'yxati")
    public ResponseEntity<?> students() {
        log.info("[CUBA Service] test/students");

        try {
            List<Map<String, Object>> items = testLegacyService.getStudentsUpdatedYesterday();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error fetching yesterday's students", e);
            return ResponseEntity.ok(null);
        }
    }
}
