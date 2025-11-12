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
import uz.hemis.common.dto.TranslationFilterRequest;
import uz.hemis.service.integration.TranslationIntegrationService;

import java.util.Map;
import java.util.UUID;

/**
 * Translation/Transcript Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/translate/*} and {@code /services/transcript/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class TranslationServiceController {

    private final TranslationIntegrationService translationIntegrationService;

    @Tag(name = "Tarjimalar")
    @Operation(summary = "Barcha tarjimalarni olish", description = "Tizim tarjimalarini to'liq ro'yxatini olish")
    @GetMapping("/translate/get")
    public ResponseEntity<Map<String, Object>> getAllTranslations() {
        log.info("GET /services/translate/get");
        return ResponseEntity.ok(translationIntegrationService.getAllTranslations());
    }

    @Tag(name = "Tarjimalar")
    @Operation(summary = "Tarjimalarni filtrlab olish", description = "Filter parametrlari bilan tarjimalarni olish")
    @PostMapping("/translate/get")
    public ResponseEntity<Map<String, Object>> getTranslationsFiltered(
        @RequestBody(description = "Filter parametrlari")
        @org.springframework.web.bind.annotation.RequestBody(required = false) TranslationFilterRequest request
    ) {
        log.info("POST /services/translate/get - lang: {}", request != null ? request.getLanguage() : "all");
        return ResponseEntity.ok(translationIntegrationService.getTranslationsFiltered(request));
    }

    @Tag(name = "Transkript")
    @Operation(summary = "Transkript ariza", description = "Talaba transkript ariza ma'lumotlarini olish")
    @GetMapping("/transcript/get")
    public ResponseEntity<Map<String, Object>> getTranscript(
        @Parameter(description = "Ariza ID", required = true) @RequestParam UUID applicationId
    ) {
        log.info("GET /services/transcript/get - applicationId: {}", applicationId);
        return ResponseEntity.ok(translationIntegrationService.getTranscript(applicationId));
    }
}
