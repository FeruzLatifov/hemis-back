package uz.hemis.api.legacy.controller.integration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.shared.BimmService;

/**
 * BIMM Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with BIMM (Birlashgan Ijtimoiy Ma'lumotlar Markazi)</p>
 * <p>OLD-HEMIS response formatiga 100% mos — raw proxy (no wrapping)</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/bimm")
@Tag(name = "66.BIMM", description = "BIMM integratsiya xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class BimmServiceController {

    private final BimmService bimmService;

    /**
     * Check disability status from BIMM
     */
    @GetMapping("/disabilityCheck")
    @Operation(summary = "Nogironlik holatini tekshirish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> disabilityCheck(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl,
            @Parameter(description = "Hujjat raqami", required = false) @RequestParam(required = false) String document) {
        log.info("[CUBA Service] bimm/disabilityCheck: pinfl={}, document={}", pinfl, document);
        return ResponseEntity.ok(bimmService.disabilityCheck(pinfl, document));
    }

    /**
     * Check poverty register status
     */
    @GetMapping("/provertyRegister")
    @Operation(summary = "Kam ta'minlangan oilalar ro'yxatini tekshirish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> provertyRegister(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/provertyRegister: pinfl={}", pinfl);
        return ResponseEntity.ok(bimmService.provertyRegister(pinfl));
    }

    /**
     * Get certificate information
     */
    @GetMapping("/certificate")
    @Operation(summary = "Sertifikat ma'lumotlarini olish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> certificate(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/certificate: pinfl={}", pinfl);
        return ResponseEntity.ok(bimmService.certificate(pinfl));
    }

    /**
     * Get academic degree information
     */
    @GetMapping("/academicDegree")
    @Operation(summary = "Ilmiy daraja ma'lumotlarini olish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> academicDegree(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/academicDegree: pinfl={}", pinfl);
        return ResponseEntity.ok(bimmService.academicDegree(pinfl));
    }

    /**
     * Get teacher training information
     */
    @GetMapping("/teacherTraining")
    @Operation(summary = "O'qituvchi malaka oshirish ma'lumotlarini olish")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> teacherTraining(
            @Parameter(description = "PINFL", required = true) @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/teacherTraining: pinfl={}", pinfl);
        return ResponseEntity.ok(bimmService.teacherTraining(pinfl));
    }
}
