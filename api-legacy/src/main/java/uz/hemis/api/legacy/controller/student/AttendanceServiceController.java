package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Attendance Service Controller - CUBA REST API Compatible
 *
 * <p>OLD-HEMIS: AttendanceServiceBean.test()</p>
 * <p>This is a test/demo endpoint that was used for entity import testing in old-hemis.</p>
 * <p>Returns static demo data (old-hemis also returned hardcoded data).</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/attendance")
@Tag(name = "09.Davomat", description = "Davomat xizmatlari")
@Slf4j
public class AttendanceServiceController {

    /**
     * Test endpoint (demo data)
     *
     * <p>OLD-HEMIS: AttendanceServiceBean.test()</p>
     * <p>Returns hardcoded demo ETranslation entities (same as old-hemis)</p>
     */
    @GetMapping("/test")
    @Operation(summary = "Test (demo data)", description = "Davomat tizimi test endpointi. Demo ma'lumotlarni qaytaradi.")
    public ResponseEntity<?> test() {
        log.info("[CUBA Service] attendance/test");

        // Old-hemis returned hardcoded ETranslation entities as import demo
        List<Map<String, Object>> items = new ArrayList<>();

        Map<String, Object> item1 = new LinkedHashMap<>();
        item1.put("_entityName", "hemishe_ETranslation");
        item1.put("id", "b1bab811-a3d6-bd21-61a7-767603a92d2d");
        item1.put("ru_Ru", "ru1");
        item1.put("message", "msg1");
        item1.put("version", 1);
        item1.put("createdBy", "otm999");
        item1.put("createTs", "2024-02-23 14:46:00.424");
        item1.put("category", "cat1");
        item1.put("updateTs", "2024-02-23 14:46:00.424");
        items.add(item1);

        Map<String, Object> item2 = new LinkedHashMap<>();
        item2.put("_entityName", "hemishe_ETranslation");
        item2.put("id", "28ffbf2d-29d4-99fc-d175-7d4a2c66502e");
        item2.put("ru_Ru", "ru2");
        item2.put("message", "msg2");
        item2.put("version", 1);
        item2.put("createdBy", "otm999");
        item2.put("createTs", "2024-02-23 14:46:00.425");
        item2.put("category", "cat2");
        item2.put("updateTs", "2024-02-23 14:46:00.425");
        items.add(item2);

        return ResponseEntity.ok(items);
    }
}
