package uz.hemis.api.legacy.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.system.TranslationFilterRequest;
import uz.hemis.service.legacy.TranslationLegacyService;

import java.util.*;

/**
 * Translation/Transcript Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/translate/*} and {@code /app/rest/v2/services/transcript/*}</p>
 *
 * @since 2.0.0
 */
@Tag(name = "14.Tarjima", description = "Tizim tarjimalari - UI va xabarlar tarjimasi")
@RestController
@RequestMapping("/app/rest/v2/services")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class TranslationServiceController {

    private final TranslationLegacyService translationService;

    @GetMapping("/translate/get")
    @Operation(
        summary = "Barcha tarjimalar",
        description = "Tizimda mavjud barcha tarjimalarni OLD-HEMIS formatida qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"success\":true,\"translations\":[{\"_entityName\":\"hemishe_ETranslation\",\"id\":\"uuid\",\"message\":\"...\",\"uz_Uz\":\"...\"}]}")))
    })
    public ResponseEntity<Map<String, Object>> getAllTranslations() {
        log.info("GET /services/translate/get - barcha tarjimalar");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        List<Map<String, Object>> translations = translationService.loadTranslations(null);
        response.put("translations", translations);

        log.info("Jami {} ta tarjima topildi", translations.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate/get")
    @Operation(
        summary = "Tarjimalarni filtrlab olish",
        description = "Filter parametrlari bilan tarjimalarni olish"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    })
    public ResponseEntity<Map<String, Object>> getTranslationsFiltered(
        @RequestBody(description = "Filter parametrlari")
        @org.springframework.web.bind.annotation.RequestBody(required = false) TranslationFilterRequest request
    ) {
        log.info("POST /services/translate/get - filter: {}", request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        List<Map<String, Object>> translations = translationService.loadTranslations(request);
        response.put("translations", translations);

        log.info("Filterdan keyin {} ta tarjima topildi", translations.size());
        return ResponseEntity.ok(response);
    }

    @Tag(name = "54.Transkript", description = "Transkript va o'quv natijalari")
    @GetMapping("/transcript/get")
    @Operation(
        summary = "Transkript ariza",
        description = "Talaba transkript ariza ma'lumotlarini olish"
    )
    public ResponseEntity<Map<String, Object>> getTranscript(
        @Parameter(description = "Ariza ID", required = true) @RequestParam UUID applicationId
    ) {
        log.info("GET /services/transcript/get - applicationId: {}", applicationId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("transcript", Map.of("id", applicationId));

        return ResponseEntity.ok(response);
    }

}
