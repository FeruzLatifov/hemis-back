package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Student REST Controller - Legacy Compatibility
 *
 * <p><strong>Legacy Endpoint from old-hemis:</strong></p>
 * <p>Preserves exact response format from CUBA-based old-hemis system</p>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /student/id - Get student ID (test endpoint)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "Legacy Entity APIs - Student", description = "Legacy student REST endpoints from old-hemis")
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Slf4j
public class StudentRestController {

    /**
     * Get student ID - Legacy test endpoint
     *
     * <p><strong>Legacy Compatibility:</strong> GET /student/id from old-hemis</p>
     * <p>Returns hardcoded test data. Original implementation in
     * {@code com.company.hemishe.rest.controller.StudentController}</p>
     *
     * @return JSON with success flag and test student ID
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/id")
    @Operation(
        summary = "Get student ID (test endpoint)",
        description = "Legacy test endpoint that returns hardcoded student ID. " +
                      "Preserved for backward compatibility with old-hemis CUBA platform."
    )
    public Map<String, Object> getId() {
        log.debug("GET /student/id - legacy test endpoint called");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("id", 123456);

        return response;
    }
}
