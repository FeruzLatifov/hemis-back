package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.ClassifierIntegrationService;

import java.util.List;
import java.util.Map;

/**
 * Classifier Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/classifiers/*}</p>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Provides access to system classifiers (справочники)</li>
 *   <li>Used by frontend for dropdowns, filters, etc.</li>
 *   <li>Includes country, region, district, citizenship, gender, etc.</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "Klassifikatorlar", description = "Tizim klassifikatorlari (справочники) - davlat, viloyat, tuman, va boshqalar")
@RestController
@RequestMapping("/services/classifiers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ClassifierServiceController {

    private final ClassifierIntegrationService classifierIntegrationService;

    /**
     * Get all classifiers
     *
     * <p><strong>Endpoint:</strong> GET /services/classifiers/allItems</p>
     *
     * @return Map of all classifier categories with their items
     */
    @Operation(
        summary = "Barcha klassifikatorlar",
        description = "Barcha klassifikatorlar ro'yxatini kategoriyalar bo'yicha olish"
    )
    @GetMapping("/allItems")
    public ResponseEntity<Map<String, List<?>>> getAllItems() {
        log.info("GET /services/classifiers/allItems");
        return ResponseEntity.ok(classifierIntegrationService.getAllItems());
    }

    /**
     * Get classifier category list
     *
     * <p><strong>Endpoint:</strong> GET /services/classifiers/info</p>
     *
     * @return List of available classifier categories
     */
    @Operation(
        summary = "Klassifikator kategoriyalari",
        description = "Mavjud klassifikator kategoriyalarining ro'yxatini olish"
    )
    @GetMapping("/info")
    public ResponseEntity<List<String>> getInfo() {
        log.info("GET /services/classifiers/info");
        return ResponseEntity.ok(classifierIntegrationService.getCategoryList());
    }

    /**
     * Get single classifier by name
     *
     * <p><strong>Endpoint:</strong> GET /services/classifiers/single</p>
     *
     * @param classifier Classifier name (e.g., "country", "region", "gender")
     * @return List of classifier items
     */
    @Operation(
        summary = "Bitta klassifikatorni olish",
        description = "Nomi bo'yicha bitta klassifikator ma'lumotlarini olish"
    )
    @GetMapping("/single")
    public ResponseEntity<List<?>> getSingle(
        @Parameter(description = "Klassifikator nomi", required = true, example = "country")
        @RequestParam String classifier
    ) {
        log.info("GET /services/classifiers/single - classifier: {}", classifier);
        return ResponseEntity.ok(classifierIntegrationService.getSingleClassifier(classifier));
    }

    /**
     * Get hokimiyat classifiers (regions/districts)
     *
     * <p><strong>Endpoint:</strong> GET /services/classifiers/hokimiyat</p>
     *
     * @return Hierarchical structure of regions and districts
     */
    @Operation(
        summary = "Hokimiyat klassifikatorlari",
        description = "Viloyat va tumanlar ierarxik strukturasini olish"
    )
    @GetMapping("/hokimiyat")
    public ResponseEntity<Map<String, Object>> getHokimiyat() {
        log.info("GET /services/classifiers/hokimiyat");
        return ResponseEntity.ok(classifierIntegrationService.getHokimiyatClassifiers());
    }
}
