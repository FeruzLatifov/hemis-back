package uz.hemis.api.legacy.controller.classifier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.legacy.ClassifierLegacyService;

import java.util.*;

/**
 * Classifier Services Controller - CUBA Service Pattern
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/services/classifiers</li>
 *   <li>Matches OLD-HEMIS CUBA service pattern</li>
 *   <li>200+ universities depend on these endpoints</li>
 * </ul>
 *
 * <p>Business logic delegated to {@link ClassifierLegacyService}</p>
 *
 * @since 1.0.0
 */
@Tag(name = "13.Klassifikatorlar", description = "Tizim klassifikatorlari - OTM, jins, fuqarolik va boshqalar")
@RestController
@RequestMapping("/app/rest/v2/services/classifiers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ClassifierServicesController {

    private final ClassifierLegacyService classifierService;

    /**
     * Barcha klassifikatorlar (items bilan)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/allItems</p>
     */
    @GetMapping("/allItems")
    @Operation(
        summary = "Barcha klassifikatorlar (items bilan)",
        description = "Tizimda mavjud barcha klassifikatorlarni title, version, count va items bilan qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"success\":true,\"classifiers\":[{\"h_gender\":{\"title\":\"Jinslar\",\"version\":4,\"count\":4,\"items\":[...]}}]}")))
    })
    public ResponseEntity<Map<String, Object>> allItems() {
        log.info("GET /services/classifiers/allItems - barcha klassifikatorlar (items bilan)");
        return ResponseEntity.ok(classifierService.getAllClassifiersWithItems());
    }

    /**
     * Barcha klassifikatorlar ro'yxati (metadata only)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/info</p>
     */
    @GetMapping("/info")
    @Operation(
        summary = "Barcha klassifikatorlar ro'yxati",
        description = "Tizimda mavjud barcha klassifikatorlar ro'yxatini title, version va count bilan qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"success\":true,\"classifiers\":[{\"h_gender\":{\"title\":\"Jinslar\",\"version\":4,\"count\":4}}]}")))
    })
    public ResponseEntity<Map<String, Object>> info() {
        log.info("GET /services/classifiers/info - barcha klassifikatorlar");
        return ResponseEntity.ok(classifierService.getAllClassifiersInfo());
    }

    /**
     * Bitta klassifikatorni olish
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/single?classifier=h_university</p>
     */
    @GetMapping("/single")
    @Operation(
        summary = "Bitta klassifikatorni olish",
        description = "Klassifikator nomi bo'yicha barcha elementlarni OLD-HEMIS formatida qaytaradi"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"success\":true,\"classifier\":{\"h_university\":{\"title\":\"...\",\"version\":1,\"count\":238,\"items\":[...]}}}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri klassifikator nomi")
    })
    public ResponseEntity<Map<String, Object>> single(
            @Parameter(description = "Klassifikator nomi", required = true, example = "h_university")
            @RequestParam String classifier
    ) {
        log.info("GET /services/classifiers/single - classifier: {}", classifier);

        Map<String, Object> result = classifierService.getSingleClassifier(classifier);
        if (result == null) {
            log.warn("Klassifikator topilmadi yoki jadval mavjud emas: {}", classifier);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Klassifikator topilmadi: " + classifier);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Hokimiyat klassifikatorlari
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/hokimiyat</p>
     */
    @GetMapping("/hokimiyat")
    @Operation(summary = "Hokimiyat klassifikatorlari", description = "Old-hemis formatida 20 ta hokimiyat klassifikatorini qaytaradi")
    public ResponseEntity<Map<String, Object>> hokimiyat() {
        log.info("GET /services/classifiers/hokimiyat");
        return ResponseEntity.ok(classifierService.getHokimiyatClassifiers());
    }

    /**
     * Hokimiyat klassifikatorlari (metadata only)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/hokimiyatInfo</p>
     */
    @GetMapping("/hokimiyatInfo")
    @Operation(summary = "Hokimiyat klassifikatorlari (info)", description = "20 ta hokimiyat klassifikatorini faqat metadata (title, version, count) bilan qaytaradi")
    public ResponseEntity<Map<String, Object>> hokimiyatInfo() {
        log.info("GET /services/classifiers/hokimiyatInfo");
        return ResponseEntity.ok(classifierService.getHokimiyatClassifiersInfo());
    }

    /**
     * Stipendiya klassifikatorlari (items bilan)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/stipend</p>
     */
    @GetMapping("/stipend")
    @Operation(summary = "Stipendiya klassifikatorlari", description = "13 ta stipendiya klassifikatorini items bilan qaytaradi")
    public ResponseEntity<Map<String, Object>> stipend() {
        log.info("GET /services/classifiers/stipend");
        return ResponseEntity.ok(classifierService.getStipendClassifiers());
    }

    /**
     * Stipendiya klassifikatorlari (metadata only)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/stipendInfo</p>
     */
    @GetMapping("/stipendInfo")
    @Operation(summary = "Stipendiya klassifikatorlari (info)", description = "13 ta stipendiya klassifikatorini faqat metadata (title, version, count) bilan qaytaradi")
    public ResponseEntity<Map<String, Object>> stipendInfo() {
        log.info("GET /services/classifiers/stipendInfo");
        return ResponseEntity.ok(classifierService.getStipendClassifiersInfo());
    }
}
