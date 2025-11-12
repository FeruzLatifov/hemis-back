package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.GraduateListRequest;
import uz.hemis.service.integration.EmploymentIntegrationService;

import java.util.Map;

/**
 * Employment Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/employment/*}</p>
 *
 * @since 2.0.0
 */
@Tag(name = "Bandlik", description = "Bitiruvchilar bandligi ma'lumotlari")
@RestController
@RequestMapping("/services/employment")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class EmploymentServiceController {

    private final EmploymentIntegrationService employmentIntegrationService;

    @Operation(summary = "Bitiruvchilar ro'yxatini yuborish", description = "Bandlik statistikasi uchun bitiruvchilar ro'yxatini yuborish")
    @PostMapping("/graduateList")
    public ResponseEntity<Map<String, Object>> submitGraduateList(
        @RequestBody(description = "Bitiruvchilar ro'yxati", required = true)
        @org.springframework.web.bind.annotation.RequestBody GraduateListRequest request
    ) {
        log.info("POST /services/employment/graduateList - count: {}", 
            request.getGraduates() != null ? request.getGraduates().size() : 0);
        return ResponseEntity.ok(employmentIntegrationService.submitGraduateList(request));
    }

    @Operation(summary = "Mehnat daftarchasi ma'lumotlari", description = "PINFL orqali mehnat daftarchasi ma'lumotlarini olish")
    @GetMapping("/workbook")
    public ResponseEntity<Map<String, Object>> getWorkbook(
        @Parameter(description = "PINFL", required = true, example = "31503776560016") @RequestParam String pinfl
    ) {
        log.info("GET /services/employment/workbook - pinfl: {}", pinfl);
        return ResponseEntity.ok(employmentIntegrationService.getWorkbook(pinfl));
    }
}
