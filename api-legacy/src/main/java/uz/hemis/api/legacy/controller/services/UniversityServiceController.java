package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.UniversityService;

/**
 * University Service Controller - CUBA REST API Compatible
 *
 * <p>Preserves exact URLs from old HEMIS CUBA platform for backward compatibility</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/university/*}</p>
 *
 * <p><strong>Methods:</strong></p>
 * <ul>
 *   <li>config - Get university configuration</li>
 *   <li>get - Get university by code</li>
 * </ul>
 *
 * @since 2.0.0
 * @see UniversityService
 */
@RestController
@RequestMapping("/services/university")
@Tag(name = "University Service API", description = "CUBA compatible university service endpoints")
@RequiredArgsConstructor
@Slf4j
public class UniversityServiceController {

    private final UniversityService universityService;

    /**
     * Get university configuration
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/university/config}</p>
     *
     * <p>Returns system-wide university configuration including:</p>
     * <ul>
     *   <li>Available universities list</li>
     *   <li>System settings</li>
     *   <li>Feature flags</li>
     * </ul>
     *
     * @return university configuration data
     */
    @GetMapping("/config")
    @Operation(
        summary = "Get university configuration",
        description = "Returns system-wide university configuration and settings"
    )
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getConfig() {
        log.info("[CUBA Service] university/config");
        return ResponseEntity.ok(universityService.getConfig());
    }

    /**
     * Get university by code
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/university/get}</p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>code - University unique code (e.g., "00001", "00002")</li>
     * </ul>
     *
     * @param code university code
     * @return university details
     */
    @GetMapping("/get")
    @Operation(
        summary = "Get university by code",
        description = "Returns detailed university information by unique code"
    )
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getByCode(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String code) {
        log.info("[CUBA Service] university/get: code={}", code);
        return ResponseEntity.ok(universityService.getByCode(code));
    }
}
