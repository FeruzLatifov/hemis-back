package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.ContractIntegrationService;

import java.util.Map;
import java.util.UUID;

/**
 * Contract Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/contract/*}</p>
 *
 * @since 2.0.0
 */
@Tag(name = "Shartnoma", description = "Talaba shartnoma ma'lumotlari")
@RestController
@RequestMapping("/services/contract")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ContractServiceController {

    private final ContractIntegrationService contractIntegrationService;

    @Operation(summary = "Shartnoma ma'lumotlarini olish", description = "Talaba ID orqali shartnoma ma'lumotlarini olish")
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getContract(
        @Parameter(description = "Talaba UUID", required = true) @RequestParam UUID studentId
    ) {
        log.info("GET /services/contract/get - studentId: {}", studentId);
        return ResponseEntity.ok(contractIntegrationService.getContract(studentId));
    }
}
