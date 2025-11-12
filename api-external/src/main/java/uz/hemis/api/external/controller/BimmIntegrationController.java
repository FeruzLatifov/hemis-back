package uz.hemis.api.external.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BIMM Integration Controller
 *
 * <p><strong>BIMM:</strong> Birlashgan Ijtimoiy Ma'lumotlar Markazi
 * (Unified Social Information Center)</p>
 *
 * <p><strong>Integration Type:</strong> S2S (System-to-System)</p>
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Migrated from old-hemis rest-services.xml</li>
 *   <li>URL Pattern: /app/rest/v2/services/bimm/*</li>
 *   <li>Used for social benefits and disability verification</li>
 * </ul>
 *
 * <p><strong>Available Services:</strong></p>
 * <ul>
 *   <li>disabilityCheck - Verify disability status via PINFL</li>
 *   <li>provertyRegister - Check poverty register enrollment</li>
 *   <li>certificate - Retrieve social certificates</li>
 *   <li>academicDegree - Verify academic degree credentials</li>
 *   <li>teacherTraining - Check teacher training certifications</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "04. External Integration APIs - BIMM", description = "Integration with Unified Social Information Center for social benefits verification")
@RestController
@RequestMapping("/app/rest/v2/services/bimm")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class BimmIntegrationController {

    /**
     * Check disability status
     *
     * <p><strong>Legacy Endpoint:</strong> POST /app/rest/v2/services/bimm/disabilityCheck</p>
     * <p>Verifies if a person has registered disability status via PINFL</p>
     *
     * @param pinfl citizen's PINFL (Personal Identification Number)
     * @param document document number for verification
     * @return disability status information
     */
    @PostMapping("/disabilityCheck")
    @Operation(
        summary = "Check disability status",
        description = "Verifies disability status in BIMM registry using PINFL and document number. " +
                      "Used for scholarship and benefit eligibility checks."
    )
    public ResponseEntity<Map<String, Object>> disabilityCheck(
            @Parameter(description = "PINFL (14-digit personal identification number)", required = true)
            @RequestParam String pinfl,
            @Parameter(description = "Document number for verification", required = true)
            @RequestParam String document
    ) {
        log.info("BIMM disabilityCheck - pinfl: {}, document: {}", pinfl, document);

        // TODO: Implement BIMM API integration
        // - Connect to BIMM web service
        // - Send disability verification request
        // - Parse and return response

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", "pending");
        response.put("message", "BIMM integration not yet implemented");
        response.put("pinfl", pinfl);
        response.put("hasDisability", false);

        return ResponseEntity.ok(response);
    }

    /**
     * Check poverty register enrollment
     *
     * <p><strong>Legacy Endpoint:</strong> POST /app/rest/v2/services/bimm/provertyRegister</p>
     * <p>Checks if a person is registered in the poverty assistance program</p>
     *
     * @param pinfl citizen's PINFL
     * @return poverty register status
     */
    @PostMapping("/provertyRegister")
    @Operation(
        summary = "Check poverty register",
        description = "Verifies enrollment in poverty assistance program. " +
                      "Used for tuition fee exemption and social scholarship eligibility."
    )
    public ResponseEntity<Map<String, Object>> provertyRegister(
            @Parameter(description = "PINFL (14-digit personal identification number)", required = true)
            @RequestParam String pinfl
    ) {
        log.info("BIMM provertyRegister - pinfl: {}", pinfl);

        // TODO: Implement BIMM API integration
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", "pending");
        response.put("message", "BIMM integration not yet implemented");
        response.put("pinfl", pinfl);
        response.put("inPovertyRegister", false);

        return ResponseEntity.ok(response);
    }

    /**
     * Get social certificate information
     *
     * <p><strong>Legacy Endpoint:</strong> POST /app/rest/v2/services/bimm/certificate</p>
     * <p>Retrieves social certificates and benefit documents</p>
     *
     * @param pinfl citizen's PINFL
     * @return certificate information
     */
    @PostMapping("/certificate")
    @Operation(
        summary = "Get certificate info",
        description = "Retrieves social certificates and benefit documentation from BIMM. " +
                      "Returns list of all registered certificates for the citizen."
    )
    public ResponseEntity<Map<String, Object>> certificate(
            @Parameter(description = "PINFL (14-digit personal identification number)", required = true)
            @RequestParam String pinfl
    ) {
        log.info("BIMM certificate - pinfl: {}", pinfl);

        // TODO: Implement BIMM API integration
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", "pending");
        response.put("message", "BIMM integration not yet implemented");
        response.put("pinfl", pinfl);
        response.put("certificates", new java.util.ArrayList<>());

        return ResponseEntity.ok(response);
    }

    /**
     * Check academic degree
     *
     * <p><strong>Legacy Endpoint:</strong> POST /app/rest/v2/services/bimm/academicDegree</p>
     * <p>Verifies academic degree credentials (PhD, DSc, etc.)</p>
     *
     * @param pinfl citizen's PINFL
     * @return academic degree information
     */
    @PostMapping("/academicDegree")
    @Operation(
        summary = "Check academic degree",
        description = "Verifies academic degree credentials in BIMM registry. " +
                      "Used for teacher qualification verification and research grants."
    )
    public ResponseEntity<Map<String, Object>> academicDegree(
            @Parameter(description = "PINFL (14-digit personal identification number)", required = true)
            @RequestParam String pinfl
    ) {
        log.info("BIMM academicDegree - pinfl: {}", pinfl);

        // TODO: Implement BIMM API integration
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", "pending");
        response.put("message", "BIMM integration not yet implemented");
        response.put("pinfl", pinfl);
        response.put("hasDegree", false);
        response.put("degrees", new java.util.ArrayList<>());

        return ResponseEntity.ok(response);
    }

    /**
     * Check teacher training certifications
     *
     * <p><strong>Legacy Endpoint:</strong> POST /app/rest/v2/services/bimm/teacherTraining</p>
     * <p>Verifies teacher training and professional development certificates</p>
     *
     * @param pinfl citizen's PINFL
     * @return teacher training information
     */
    @PostMapping("/teacherTraining")
    @Operation(
        summary = "Check teacher training",
        description = "Verifies teacher training certifications and professional development courses. " +
                      "Used for teacher qualification requirements and hiring eligibility."
    )
    public ResponseEntity<Map<String, Object>> teacherTraining(
            @Parameter(description = "PINFL (14-digit personal identification number)", required = true)
            @RequestParam String pinfl
    ) {
        log.info("BIMM teacherTraining - pinfl: {}", pinfl);

        // TODO: Implement BIMM API integration
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", "pending");
        response.put("message", "BIMM integration not yet implemented");
        response.put("pinfl", pinfl);
        response.put("trainings", new java.util.ArrayList<>());

        return ResponseEntity.ok(response);
    }
}
