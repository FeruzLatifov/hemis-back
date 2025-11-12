package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.ExternalIntegrationService;

import java.util.Map;

/**
 * External Integration Services
 *
 * <p>DTM Mandat, OAK, Tax, UzASBO integrations</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ExternalIntegrationController {

    private final ExternalIntegrationService externalIntegrationService;

    @Tag(name = "DTM")
    @Operation(summary = "DTM mandat ma'lumotlari", description = "Test natijalari va mandat ma'lumotlarini olish")
    @GetMapping("/mandat/get")
    public ResponseEntity<Map<String, Object>> getDtmMandat(
        @Parameter(description = "Abituriyent ID", required = true) @RequestParam String applicantId
    ) {
        log.info("GET /services/mandat/get - applicantId: {}", applicantId);
        return ResponseEntity.ok(externalIntegrationService.getDtmMandat(applicantId));
    }

    @Tag(name = "OAK")
    @Operation(summary = "OAK ma'lumotlari", description = "Ilmiy xodim ma'lumotlarini OAK dan olish")
    @GetMapping("/oak/byPin")
    public ResponseEntity<Map<String, Object>> getOakInfo(
        @Parameter(description = "PINFL", required = true, example = "32707860270013") @RequestParam String pinfl
    ) {
        log.info("GET /services/oak/byPin - pinfl: {}", pinfl);
        return ResponseEntity.ok(externalIntegrationService.getOakInfo(pinfl));
    }

    @Tag(name = "Soliq")
    @Operation(summary = "Ijara shartnomasi", description = "Soliq ma'lumotlari - ijara shartnomasi")
    @GetMapping("/tax/rent")
    public ResponseEntity<Map<String, Object>> getTaxRent(
        @Parameter(description = "Shartnoma raqami", required = true) @RequestParam String contractNumber
    ) {
        log.info("GET /services/tax/rent - contractNumber: {}", contractNumber);
        return ResponseEntity.ok(externalIntegrationService.getTaxRent(contractNumber));
    }

    @Tag(name = "UzASBO")
    @Operation(summary = "UzASBO stipendiya", description = "UzASBO tizimidan stipendiya ma'lumotlarini olish")
    @GetMapping("/uzasbo/scholarship")
    public ResponseEntity<Map<String, Object>> getUzasboScholarship(
        @Parameter(description = "Talaba ID", required = true) @RequestParam String studentId,
        @Parameter(description = "Davr (yil-oy)", example = "2024-09") @RequestParam(required = false) String period
    ) {
        log.info("GET /services/uzasbo/scholarship - studentId: {}, period: {}", studentId, period);
        return ResponseEntity.ok(externalIntegrationService.getUzasboScholarship(studentId, period));
    }
}
