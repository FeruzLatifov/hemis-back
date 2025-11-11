package uz.hemis.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.app.service.base.AbstractInternalCubaService;
import uz.hemis.domain.entity.Classifier;
import uz.hemis.domain.repository.ClassifierRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classifiers CUBA Service - Reference Data
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Provides reference/classifier data for dropdowns and lookups</li>
 *   <li>Student status, gender, citizenship, education types, etc.</li>
 *   <li>Used by university frontends for form dropdowns</li>
 * </ul>
 *
 * <p><strong>OPTIMIZATION:</strong></p>
 * <ul>
 *   <li>Extends AbstractInternalCubaService</li>
 *   <li>Uses REAL database via ClassifierRepository</li>
 *   <li>No mock data - 100% production ready</li>
 * </ul>
 *
 * <p><strong>Methods (7):</strong></p>
 * <ul>
 *   <li>single - Get items for single classifier</li>
 *   <li>allItems - Get all classifiers (metadata)</li>
 *   <li>info - Get classifier structure info</li>
 *   <li>stipend - Get stipend types</li>
 *   <li>stipendInfo - Get stipend classifier info</li>
 *   <li>hokimiyat - Get hokimiyat data</li>
 *   <li>hokimiyatInfo - Get hokimiyat info</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS URL Pattern:</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_ClassifiersService/single?classifier={name}
 * GET /app/rest/v2/services/hemishe_ClassifiersService/allItems
 * GET /app/rest/v2/services/hemishe_ClassifiersService/info
 * </pre>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassifiersCubaService extends AbstractInternalCubaService {

    private final ClassifierRepository classifierRepository;

    /**
     * Get items for single classifier
     *
     * <p><strong>Method:</strong> single</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_ClassifiersService/single?classifier={name}</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>classifier=StudentStatus → Student statuses (11, 12, 21, 31, etc.)</li>
     *   <li>classifier=Gender → Male/Female (1, 2)</li>
     *   <li>classifier=EducationType → Bakalavr, Magistratura, etc. (11, 12, 13)</li>
     * </ul>
     *
     * @param classifier Classifier name
     * @return Classifier items list
     */
    public Map<String, Object> single(String classifier) {
        log.info("Getting classifier items - Classifier: {}", classifier);

        Map<String, Object> validationError = validateRequired("classifier", classifier);
        if (validationError != null) {
            return validationError;
        }

        // Query from REAL database
        List<Classifier> classifiers = classifierRepository.findAllByType(classifier);

        List<Map<String, Object>> items = classifiers.stream()
                .map(this::mapClassifierToResponse)
                .collect(Collectors.toList());

        log.info("Found {} items for classifier: {}", items.size(), classifier);
        return successListResponse(items);
    }

    /**
     * Get all classifiers metadata
     *
     * <p><strong>Method:</strong> allItems</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_ClassifiersService/allItems</p>
     *
     * <p>Returns list of all available classifiers with their names and codes</p>
     *
     * @return All classifiers list
     */
    public Map<String, Object> allItems() {
        log.info("Getting all classifiers metadata");

        // Query all classifier types from REAL database
        List<String> types = classifierRepository.findAllTypes();

        List<Map<String, Object>> classifiers = types.stream()
                .map(type -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", UUID.randomUUID());
                    item.put("code", type);
                    item.put("name", type);
                    item.put("count", classifierRepository.countByType(type));
                    return item;
                })
                .collect(Collectors.toList());

        log.info("Found {} classifier types", classifiers.size());
        return successListResponse(classifiers);
    }

    /**
     * Get classifiers structure info
     *
     * <p><strong>Method:</strong> info</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_ClassifiersService/info</p>
     *
     * <p>Returns metadata about classifier system structure</p>
     *
     * @return Classifier system info
     */
    public Map<String, Object> info() {
        log.info("Getting classifiers info");

        // Count from REAL database
        long totalClassifiers = classifierRepository.count();
        List<String> types = classifierRepository.findAllTypes();

        Map<String, Object> info = new HashMap<>();
        info.put("total_classifiers", totalClassifiers);
        info.put("total_types", types.size());
        info.put("version", "2.0 - Spring Boot");
        info.put("last_updated", new Date());

        return successResponse(info);
    }

    /**
     * Get stipend types
     *
     * <p><strong>Method:</strong> stipend</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_ClassifiersService/stipend</p>
     *
     * @return Stipend types list
     */
    public Map<String, Object> stipend() {
        log.info("Getting stipend types");

        // Query from REAL database - stipend types classifier
        List<Classifier> stipendTypes = classifierRepository.findAllByType("STIPEND_TYPE");

        List<Map<String, Object>> items = stipendTypes.stream()
                .map(this::mapClassifierToResponse)
                .collect(Collectors.toList());

        log.info("Found {} stipend types", items.size());
        return successListResponse(items);
    }

    /**
     * Get stipend classifier info
     *
     * <p><strong>Method:</strong> stipendInfo</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_ClassifiersService/stipendInfo</p>
     *
     * @return Stipend classifier metadata
     */
    public Map<String, Object> stipendInfo() {
        log.info("Getting stipend info");

        long totalTypes = classifierRepository.countByType("STIPEND_TYPE");

        Map<String, Object> info = new HashMap<>();
        info.put("total_types", totalTypes);
        info.put("description", "Stipendiya turlari");

        return successResponse(info);
    }

    /**
     * Get hokimiyat data
     *
     * <p><strong>Method:</strong> hokimiyat</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_ClassifiersService/hokimiyat</p>
     *
     * <p>Hokimiyat = Local government authorities (regions, districts)</p>
     *
     * @return Hokimiyat list
     */
    public Map<String, Object> hokimiyat() {
        log.info("Getting hokimiyat data");

        // Query from REAL database - hokimiyat/region classifier
        List<Classifier> hokimiyatList = classifierRepository.findAllByType("HOKIMIYAT");

        List<Map<String, Object>> items = hokimiyatList.stream()
                .map(this::mapClassifierToResponse)
                .collect(Collectors.toList());

        log.info("Found {} hokimiyat entries", items.size());
        return successListResponse(items);
    }

    /**
     * Get hokimiyat info
     *
     * <p><strong>Method:</strong> hokimiyatInfo</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_ClassifiersService/hokimiyatInfo</p>
     *
     * @return Hokimiyat classifier metadata
     */
    public Map<String, Object> hokimiyatInfo() {
        log.info("Getting hokimiyat info");

        long totalHokimiyat = classifierRepository.countByType("HOKIMIYAT");

        Map<String, Object> info = new HashMap<>();
        info.put("total_entries", totalHokimiyat);
        info.put("description", "Hududlar va tumanlar");

        return successResponse(info);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Map Classifier entity to response format
     * Preserves OLD-HEMIS response structure
     */
    private Map<String, Object> mapClassifierToResponse(Classifier classifier) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", classifier.getId());
        map.put("code", classifier.getCode());
        map.put("name", classifier.getNameUz()); // Default to Uzbek
        map.put("name_uz", classifier.getNameUz());
        map.put("name_ru", classifier.getNameRu());
        map.put("name_en", classifier.getNameEn());
        map.put("classifier_type", classifier.getClassifierType());
        map.put("description", classifier.getDescription());
        map.put("sort_order", classifier.getSortOrder());
        map.put("is_active", classifier.getIsActive());
        return map;
    }
}
