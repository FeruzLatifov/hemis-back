package uz.hemis.api.legacy.controller.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.document.DiplomaService;
import uz.hemis.service.legacy.DiplomaLegacyService;

import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Diploma Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/diploma/*}</p>
 *
 * @since 2.0.0
 */
@Tag(name = "70.Qo'shimcha xizmatlar", description = "Diploma tekshirish va ma'lumotlar xizmatlari")
@RestController
@RequestMapping("/app/rest/v2/services/diploma")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DiplomaServiceController {

    private final DiplomaService diplomaService;
    private final DiplomaLegacyService diplomaLegacyService;

    /**
     * Get diploma info by PINFL
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/diploma/info?pinfl=...</p>
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    @Operation(summary = "Diploma ma'lumotlarini PINFL orqali olish")
    public ResponseEntity<?> info(
            @Parameter(description = "Talabaning PINFL raqami")
            @RequestParam String pinfl
    ) {
        log.info("GET /services/diploma/info - pinfl: {}****", pinfl.length() > 4 ? pinfl.substring(0, 4) : pinfl);

        Map<String, Object> response = diplomaLegacyService.getDiplomaInfoByPinfl(pinfl);

        return ResponseEntity.ok(response);
    }

    /**
     * Get diploma by hash (QR code verification)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/diploma/byhash</p>
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/byhash")
    @Operation(
            summary = "Get diploma by hash",
            description = "Verifies diploma authenticity using QR code hash."
    )
    public ResponseEntity<Map<String, Object>> byHash(
            @Parameter(description = "Diploma hash from QR code")
            @RequestParam String hash
    ) {
        log.info("GET /services/diploma/byhash - hash: {}", hash);

        return diplomaService.findEntityByDiplomaHash(hash)
                .map(diploma -> {
                    log.info("Diploma verified - number: {}", diploma.getDiplomaNumber());
                    return ResponseEntity.ok(diplomaService.toDiplomaVerificationMap(diploma));
                })
                .orElseGet(() -> {
                    log.warn("Diploma not found for hash: {}", hash);
                    return ResponseEntity.notFound().build();
                });
    }
}
