package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.repository.ClassifierRepository;

import java.util.*;
import java.util.stream.Collectors;

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
 * @since 1.0.0
 */
@Tag(name = "Classifiers")
@RestController
@RequestMapping("/app/rest/v2/services/classifiers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ClassifierServicesController {

    private final ClassifierRepository classifierRepository;

    /**
     * Get all classifier items by name
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/allItems</p>
     *
     * @param name classifier type name
     * @return list of classifier items
     */
    @GetMapping("/allItems")
    @Operation(summary = "Get all classifier items", description = "Returns all items for a specific classifier type")
    public ResponseEntity<List<Map<String, Object>>> allItems(
            @Parameter(description = "Classifier type name (e.g., ECountry, ERegion)")
            @RequestParam String name
    ) {
        log.info("GET /services/classifiers/allItems - name: {}", name);

        List<Map<String, Object>> items = classifierRepository.findAllByType(name)
                .stream()
                .map(classifier -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", classifier.getId());
                    item.put("code", classifier.getCode());
                    item.put("name", classifier.getNameUz());
                    item.put("nameUz", classifier.getNameUz());
                    item.put("nameRu", classifier.getNameRu());
                    item.put("nameEn", classifier.getNameEn());
                    item.put("sortOrder", classifier.getSortOrder());
                    item.put("isActive", classifier.getIsActive());
                    return item;
                })
                .collect(Collectors.toList());

        log.info("Found {} items for classifier: {}", items.size(), name);
        return ResponseEntity.ok(items);
    }

    /**
     * Get classifier information (metadata)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/info</p>
     *
     * @param name classifier type name
     * @return classifier metadata
     */
    @GetMapping("/info")
    @Operation(summary = "Get classifier info", description = "Returns metadata about a classifier type")
    public ResponseEntity<Map<String, Object>> info(
            @Parameter(description = "Classifier type name")
            @RequestParam String name
    ) {
        log.info("GET /services/classifiers/info - name: {}", name);

        long count = classifierRepository.countByType(name);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", name);
        info.put("count", count);
        info.put("description", "Classifier: " + name);

        return ResponseEntity.ok(info);
    }

    /**
     * Get single classifier item
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/single</p>
     *
     * @param name classifier type name
     * @param code classifier code
     * @return single classifier item
     */
    @GetMapping("/single")
    @Operation(summary = "Get single classifier", description = "Returns a single classifier item by code")
    public ResponseEntity<Map<String, Object>> single(
            @Parameter(description = "Classifier type name")
            @RequestParam String name,
            @Parameter(description = "Classifier code")
            @RequestParam String code
    ) {
        log.info("GET /services/classifiers/single - name: {}, code: {}", name, code);

        return classifierRepository.findByTypeAndCode(name, code)
                .map(classifier -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", classifier.getId());
                    item.put("code", classifier.getCode());
                    item.put("name", classifier.getNameUz());
                    item.put("nameUz", classifier.getNameUz());
                    item.put("nameRu", classifier.getNameRu());
                    item.put("nameEn", classifier.getNameEn());
                    item.put("sortOrder", classifier.getSortOrder());
                    item.put("isActive", classifier.getIsActive());
                    return ResponseEntity.ok(item);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get hokimiyat (local government) classifiers
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/classifiers/hokimiyat</p>
     *
     * <p>Returns hierarchical structure of regions and districts</p>
     *
     * @return hokimiyat classifiers (regions and districts)
     */
    @GetMapping("/hokimiyat")
    @Operation(summary = "Get hokimiyat classifiers", description = "Returns local government structure (regions and districts)")
    public ResponseEntity<Map<String, Object>> hokimiyat() {
        log.info("GET /services/classifiers/hokimiyat");

        // Get regions (ERegion)
        List<Map<String, Object>> regions = classifierRepository.findAllByType("ERegion")
                .stream()
                .map(classifier -> {
                    Map<String, Object> region = new LinkedHashMap<>();
                    region.put("id", classifier.getId());
                    region.put("code", classifier.getCode());
                    region.put("name", classifier.getNameUz());
                    region.put("nameUz", classifier.getNameUz());
                    region.put("nameRu", classifier.getNameRu());
                    region.put("nameEn", classifier.getNameEn());

                    // Get districts for this region
                    // Note: Assuming district codes start with region code
                    List<Map<String, Object>> districts = classifierRepository.findAllByType("EDistrict")
                            .stream()
                            .filter(d -> d.getCode() != null && d.getCode().startsWith(classifier.getCode()))
                            .map(district -> {
                                Map<String, Object> d = new LinkedHashMap<>();
                                d.put("id", district.getId());
                                d.put("code", district.getCode());
                                d.put("name", district.getNameUz());
                                d.put("nameUz", district.getNameUz());
                                d.put("nameRu", district.getNameRu());
                                d.put("nameEn", district.getNameEn());
                                return d;
                            })
                            .collect(Collectors.toList());

                    region.put("districts", districts);
                    return region;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("regions", regions);
        result.put("totalRegions", regions.size());

        log.info("Returned {} regions with districts", regions.size());
        return ResponseEntity.ok(result);
    }
}
