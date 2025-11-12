package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Faculty Service Controller - CUBA REST API Compatible
 *
 * <p>Preserves exact URLs from old HEMIS CUBA platform for backward compatibility</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/faculty/*}</p>
 *
 * <p><strong>Legacy Methods:</strong></p>
 * <ul>
 *   <li>list - Get faculty list by university</li>
 *   <li>get - Get faculty by code</li>
 *   <li>count - Count faculties by university</li>
 * </ul>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/faculty")
@Tag(name = "Faculty Service API", description = "CUBA compatible faculty service endpoints")
@RequiredArgsConstructor
@Slf4j
public class FacultyServiceController {

    /**
     * Get faculties by university
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/faculty/list}</p>
     *
     * @param universityCode University code
     * @return List of faculties
     */
    @GetMapping("/list")
    @Operation(
        summary = "Get faculties by university",
        description = "Returns list of faculties for specific university (CUBA compatible)"
    )
    public ResponseEntity<?> list(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String universityCode) {
        log.info("[CUBA Service] faculty/list: universityCode={}", universityCode);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", java.util.List.of(),
            "message", "Legacy CUBA endpoint - use /api/v1/web/registry/faculties/by-university/{id} instead"
        ));
    }

    /**
     * Get faculty by code
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/faculty/get}</p>
     *
     * @param code Faculty code
     * @return Faculty details
     */
    @GetMapping("/get")
    @Operation(
        summary = "Get faculty by code",
        description = "Returns faculty details by code (CUBA compatible)"
    )
    public ResponseEntity<?> get(
            @Parameter(description = "Faculty code", required = true, example = "001")
            @RequestParam String code) {
        log.info("[CUBA Service] faculty/get: code={}", code);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Legacy CUBA endpoint - use /api/v1/web/registry/faculties/{id} instead"
        ));
    }

    /**
     * Count faculties by university
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/faculty/count}</p>
     *
     * @param universityCode University code
     * @return Faculty count
     */
    @GetMapping("/count")
    @Operation(
        summary = "Count faculties",
        description = "Returns count of faculties for university (CUBA compatible)"
    )
    public ResponseEntity<?> count(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String universityCode) {
        log.info("[CUBA Service] faculty/count: universityCode={}", universityCode);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", 0,
            "message", "Legacy CUBA endpoint - use new API instead"
        ));
    }
}

