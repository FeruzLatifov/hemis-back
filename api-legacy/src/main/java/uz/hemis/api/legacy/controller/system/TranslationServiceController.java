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
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Translation/Transcript Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/translate/*}</p>
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/translate/get")
    @Operation(
        summary = "Barcha tarjimalar",
        description = "Tizimda mavjud barcha tarjimalarni OLD-HEMIS formatida qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema()))
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

    /**
     * OLD-HEMIS Compatible POST endpoint.
     *
     * <p>Accepts CUBA format: {"category": "...", "messages": ["msg1", "msg2"]}</p>
     * <p>Auto-creates missing translations (old-hemis TranslationServiceBean.get(category, messages))</p>
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/translate/get")
    @Operation(
        summary = "Tarjimalarni filtrlab olish",
        description = "Filter parametrlari bilan tarjimalarni olish. Topilmagan xabarlar avtomatik yaratiladi (old-hemis compatible)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli")
    })
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getTranslationsFiltered(
        @org.springframework.web.bind.annotation.RequestBody(required = false) Map<String, Object> requestBody
    ) {
        log.info("POST /services/translate/get - request: {}", requestBody);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        List<Map<String, Object>> translations;

        // OLD-HEMIS format: {"category": "...", "messages": ["msg1", "msg2"]}
        if (requestBody != null && requestBody.containsKey("messages")) {
            String category = requestBody.get("category") != null ? requestBody.get("category").toString() : null;
            List<String> messages = (List<String>) requestBody.get("messages");
            translations = translationService.loadTranslationsWithAutoCreate(category, messages);
        } else {
            // Fallback: filter by category
            TranslationFilterRequest filter = new TranslationFilterRequest();
            if (requestBody != null && requestBody.containsKey("category")) {
                filter.setCategory(requestBody.get("category").toString());
            }
            translations = translationService.loadTranslations(filter);
        }

        response.put("translations", translations);
        log.info("Filterdan keyin {} ta tarjima topildi", translations.size());
        return ResponseEntity.ok(response);
    }

}
