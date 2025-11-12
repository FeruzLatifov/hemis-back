package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * GUVD Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with GUVD (Ichki Ishlar Vazirligi)</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/guvd/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/guvd")
@Tag(name = "GUVD Service API", description = "CUBA compatible GUVD integration service endpoints")
@RequiredArgsConstructor
@Slf4j
public class GuvdServiceController {

    /**
     * Get classifiers from GUVD
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/guvd/classifiers}</p>
     *
     * @return list of classifiers
     */
    @GetMapping("/classifiers")
    @Operation(summary = "Get GUVD classifiers", description = "Gets classifier list from GUVD system")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> classifiers() {
        log.info("[CUBA Service] guvd/classifiers");
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "classifiers", java.util.List.of(),
            "message", "GUVD integration - implementation pending"
        ));
    }

    /**
     * Get objects from GUVD
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/guvd/objects}</p>
     *
     * @return list of objects
     */
    @GetMapping("/objects")
    @Operation(summary = "Get GUVD objects", description = "Gets object list from GUVD system")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> objects() {
        log.info("[CUBA Service] guvd/objects");
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "objects", java.util.List.of(),
            "message", "GUVD integration - implementation pending"
        ));
    }
}
