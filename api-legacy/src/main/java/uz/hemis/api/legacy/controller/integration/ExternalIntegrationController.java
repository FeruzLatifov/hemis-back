package uz.hemis.api.legacy.controller.integration;

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
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * External Integration Services
 *
 * <p>Tax, UzASBO integrations</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ExternalIntegrationController {

    private final ExternalIntegrationService externalIntegrationService;

    @Tag(name = "60.Soliq")
    @Operation(summary = "Ijara shartnomasi", description = "Soliq ma'lumotlari - ijara shartnomasi. PINFL bo'yicha ijara shartnomalarini olish.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tax/rent")
    public ResponseEntity<Map<String, Object>> getTaxRent(
        @Parameter(description = "PINFL (14 xonali)", required = true, example = "51805035330018") @RequestParam String pinfl
    ) {
        log.info("GET /services/tax/rent - pinfl: {}", pinfl);
        return ResponseEntity.ok(externalIntegrationService.getTaxRent(pinfl));
    }

    @Tag(name = "58.UzASBO")
    @Operation(summary = "UzASBO stipendiya", description = "UzASBO tizimidan stipendiya ma'lumotlarini olish. INN, yil va oy bo'yicha.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/uzasbo/scholarship")
    public ResponseEntity<Map<String, Object>> getUzasboScholarship(
        @Parameter(description = "INN (tashkilot identifikatori)", required = true, example = "201354108") @RequestParam String inn,
        @Parameter(description = "Yil", required = true, example = "2023") @RequestParam Integer year,
        @Parameter(description = "Oy (1-12)", required = true, example = "9") @RequestParam Integer month
    ) {
        log.info("GET /services/uzasbo/scholarship - inn: {}, year: {}, month: {}", inn, year, month);
        return ResponseEntity.ok(externalIntegrationService.getUzasboScholarship(inn, year, month));
    }
}
