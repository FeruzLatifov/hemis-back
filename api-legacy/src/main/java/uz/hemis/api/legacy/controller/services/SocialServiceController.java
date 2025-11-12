package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Social Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with Social Protection services</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/social/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/social")
@Tag(name = "Social Service API", description = "CUBA compatible social services integration endpoints")
@RequiredArgsConstructor
@Slf4j
public class SocialServiceController {

    /**
     * Check single register status
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/singleRegister}</p>
     *
     * @param pinfl citizen PINFL
     * @return single register information
     */
    @GetMapping("/singleRegister")
    @Operation(summary = "Check single register", description = "Checks single register status")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> singleRegister(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] social/singleRegister: pinfl={}", pinfl);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "registered", false,
            "message", "Social integration - implementation pending"
        ));
    }

    /**
     * Get full daftar information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/daftarFull}</p>
     *
     * @param pinfl citizen PINFL
     * @return full daftar data
     */
    @GetMapping("/daftarFull")
    @Operation(summary = "Get full daftar", description = "Gets full daftar information")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> daftarFull(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] social/daftarFull: pinfl={}", pinfl);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "daftar", java.util.Map.of(),
            "message", "Social integration - implementation pending"
        ));
    }

    /**
     * Get short daftar information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/daftarShort}</p>
     *
     * @param pinfl citizen PINFL
     * @return short daftar data
     */
    @GetMapping("/daftarShort")
    @Operation(summary = "Get short daftar", description = "Gets short daftar information")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> daftarShort(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] social/daftarShort: pinfl={}", pinfl);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "daftar", java.util.Map.of(),
            "message", "Social integration - implementation pending"
        ));
    }

    /**
     * Get women support information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/women}</p>
     *
     * @param pinfl citizen PINFL
     * @param sn serial number
     * @return women support data
     */
    @GetMapping("/women")
    @Operation(summary = "Get women support info", description = "Gets women support information")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> women(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl,
            @Parameter(description = "Serial number", required = false)
            @RequestParam(required = false) String sn) {
        log.info("[CUBA Service] social/women: pinfl={}, sn={}", pinfl, sn);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "support", java.util.List.of(),
            "message", "Social integration - implementation pending"
        ));
    }

    /**
     * Get youth support information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/young}</p>
     *
     * @param pinfl citizen PINFL
     * @return youth support data
     */
    @GetMapping("/young")
    @Operation(summary = "Get youth support info", description = "Gets youth support information")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> young(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] social/young: pinfl={}", pinfl);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "support", java.util.List.of(),
            "message", "Social integration - implementation pending"
        ));
    }
}
