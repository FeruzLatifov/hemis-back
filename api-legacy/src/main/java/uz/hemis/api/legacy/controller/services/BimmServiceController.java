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
 * BIMM Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with BIMM (Birlashgan Ijtimoiy Ma'lumotlar Markazi)</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/bimm/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/bimm")
@Tag(name = "BIMM Service API", description = "CUBA compatible BIMM integration service endpoints")
@RequiredArgsConstructor
@Slf4j
public class BimmServiceController {

    /**
     * Check disability status from BIMM
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/disabilityCheck}</p>
     *
     * @param pinfl citizen PINFL
     * @param document document number
     * @return disability information
     */
    @GetMapping("/disabilityCheck")
    @Operation(summary = "Check disability status", description = "Checks disability status from BIMM system")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> disabilityCheck(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl,
            @Parameter(description = "Document number", required = false)
            @RequestParam(required = false) String document) {
        log.info("[CUBA Service] bimm/disabilityCheck: pinfl={}, document={}", pinfl, document);
        
        // TODO: Implement with BimmIntegrationService
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "hasDisability", false,
            "message", "BIMM integration - implementation pending"
        ));
    }

    /**
     * Check poverty register status
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/provertyRegister}</p>
     *
     * @param pinfl citizen PINFL
     * @return poverty register information
     */
    @GetMapping("/provertyRegister")
    @Operation(summary = "Check poverty register", description = "Checks if person is in poverty register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> provertyRegister(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/provertyRegister: pinfl={}", pinfl);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "inRegister", false,
            "message", "BIMM integration - implementation pending"
        ));
    }

    /**
     * Get certificate information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/certificate}</p>
     *
     * @param pinfl citizen PINFL
     * @return certificate data
     */
    @GetMapping("/certificate")
    @Operation(summary = "Get certificate", description = "Gets certificate information from BIMM")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> certificate(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/certificate: pinfl={}", pinfl);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "certificates", java.util.List.of(),
            "message", "BIMM integration - implementation pending"
        ));
    }

    /**
     * Get academic degree information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/academicDegree}</p>
     *
     * @param pinfl citizen PINFL
     * @return academic degree data
     */
    @GetMapping("/academicDegree")
    @Operation(summary = "Get academic degree", description = "Gets academic degree information from BIMM")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> academicDegree(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/academicDegree: pinfl={}", pinfl);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "degrees", java.util.List.of(),
            "message", "BIMM integration - implementation pending"
        ));
    }

    /**
     * Get teacher training information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/teacherTraining}</p>
     *
     * @param pinfl citizen PINFL
     * @return teacher training data
     */
    @GetMapping("/teacherTraining")
    @Operation(summary = "Get teacher training", description = "Gets teacher training information from BIMM")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> teacherTraining(
            @Parameter(description = "Citizen PINFL", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/teacherTraining: pinfl={}", pinfl);
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pinfl", pinfl,
            "trainings", java.util.List.of(),
            "message", "BIMM integration - implementation pending"
        ));
    }
}
