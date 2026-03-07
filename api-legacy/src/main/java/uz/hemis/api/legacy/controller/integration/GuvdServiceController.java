package uz.hemis.api.legacy.controller.integration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.service.legacy.GuvdLegacyService;

import java.util.Map;

/**
 * GUVD Service Controller - OLD-HEMIS compatible
 *
 * <p>Matches old-hemis CUBA REST service: /app/rest/v2/services/guvd</p>
 * <ul>
 *   <li>GET /classifiers — 5 classifiers (h_soato, h_gender, h_university_form, h_ownership, h_university)</li>
 *   <li>GET /objects — All universities (code, name, tin)</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Tag(name = "14.GUVD", description = "GUVD integratsiya endpointlari")
@RestController
@RequestMapping("/app/rest/v2/services/guvd")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class GuvdServiceController {

    private final GuvdLegacyService guvdLegacyService;

    /**
     * GUVD classifiers — 5 ta classifier (old-hemis compatible)
     *
     * <p>h_soato (faqat '1726%' prefix), h_gender, h_university_form, h_ownership, h_university</p>
     */
    @GetMapping("/classifiers")
    @Operation(summary = "GUVD klassifikatorlari", description = "5 ta GUVD klassifikatorini qaytaradi (old-hemis format)")
    public ResponseEntity<Map<String, Object>> classifiers() {
        log.info("GET /services/guvd/classifiers");
        return ResponseEntity.ok(guvdLegacyService.getClassifiers());
    }

    /**
     * GUVD objects — All universities (code, name, tin)
     *
     * <p>Old-hemis format: { success: true, count: N, data: [{code, name, tin}, ...] }</p>
     */
    @GetMapping("/objects")
    @Operation(summary = "Barcha universitetlar (GUVD)", description = "Barcha universitetlarni code, name, tin bilan qaytaradi (old-hemis format)")
    public ResponseEntity<Map<String, Object>> objects() {
        log.info("GET /services/guvd/objects");
        return ResponseEntity.ok(guvdLegacyService.getObjects());
    }
}
