package uz.hemis.university.classifier.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.ClassifiersCubaService;

import java.util.Map;

/**
 * Classifier API Controller
 *
 * <p><strong>Feature:</strong> System Classifiers and Dictionaries</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong> ClassifiersServiceBean.java</p>
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>System classifiers (gender, nationality, citizenship, etc.)</li>
 *   <li>Stipend/scholarship types and catalogs</li>
 *   <li>Hokimiyat (local government) registries</li>
 *   <li>Dictionary metadata and information</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern (Backward Compatible):</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_ClassifiersService/{methodName}
 * → ClassifiersCubaService.{methodName}()
 * </pre>
 *
 * <p><strong>Common Classifiers:</strong></p>
 * <ul>
 *   <li>H_GENDER - Gender types</li>
 *   <li>H_NATIONALITY - Nationalities</li>
 *   <li>H_CITIZENSHIP - Citizenship types</li>
 *   <li>H_EDUCATION_TYPE - Education levels (Bachelor, Master, PhD)</li>
 *   <li>H_PAYMENT_FORM - Payment forms (Grant, Contract)</li>
 *   <li>H_STUDENT_STATUS - Student statuses</li>
 *   <li>And 100+ more classifiers...</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong> 7 classifier management endpoints</p>
 * <p><strong>Users:</strong> 200+ universities across Uzbekistan</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/hemishe_ClassifiersService")
@RequiredArgsConstructor
@Slf4j
public class ClassifierApiController {

    private final ClassifiersCubaService classifiersCubaService;

    /**
     * Get single classifier items
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_ClassifiersService/single?classifier={name}
     * </pre>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * curl 'https://ministry.hemis.uz/app/rest/v2/services/hemishe_ClassifiersService/single?classifier=H_GENDER' \
     *   -H 'Authorization: Bearer {token}'
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "classifier": "H_GENDER",
     *   "items": [
     *     {"code": "11", "name": "Erkak", "name_latin": "Male"},
     *     {"code": "12", "name": "Ayol", "name_latin": "Female"}
     *   ]
     * }
     * </pre>
     *
     * @param classifier Classifier code (e.g., H_GENDER, H_NATIONALITY)
     * @return list of classifier items with codes and names
     */
    @GetMapping("/single")
    public ResponseEntity<Map<String, Object>> single(@RequestParam("classifier") String classifier) {
        log.info("📋 Classifier single - Classifier: {}", classifier);
        Map<String, Object> result = classifiersCubaService.single(classifier);
        log.info("✅ Classifier single completed - Items: {}",
            result.get("items") != null ? ((java.util.List<?>) result.get("items")).size() : 0);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all classifier items
     *
     * <p>Returns ALL classifiers in the system with their items</p>
     * <p><strong>Warning:</strong> Large response (~1-2 MB), use with caching</p>
     *
     * <p><strong>Response Structure:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "classifiers": {
     *     "H_GENDER": [
     *       {"code": "11", "name": "Erkak"},
     *       {"code": "12", "name": "Ayol"}
     *     ],
     *     "H_NATIONALITY": [...],
     *     "H_CITIZENSHIP": [...],
     *     ...
     *   },
     *   "total_classifiers": 120
     * }
     * </pre>
     *
     * @return all classifiers with their items
     */
    @GetMapping("/allItems")
    public ResponseEntity<Map<String, Object>> allItems() {
        log.info("📚 Classifier allItems - Fetching all classifiers");
        Map<String, Object> result = classifiersCubaService.allItems();
        log.info("✅ Classifier allItems completed - Count: {}", result.get("total_classifiers"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get classifiers information (metadata)
     *
     * <p>Returns metadata about available classifiers without their items</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "classifiers": [
     *     {
     *       "code": "H_GENDER",
     *       "name": "Jins",
     *       "description": "Gender types",
     *       "item_count": 2
     *     },
     *     {
     *       "code": "H_NATIONALITY",
     *       "name": "Millat",
     *       "description": "Nationality types",
     *       "item_count": 130
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @return classifier metadata list
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        log.info("ℹ️ Classifier info - Fetching metadata");
        Map<String, Object> result = classifiersCubaService.info();
        log.info("✅ Classifier info completed");
        return ResponseEntity.ok(result);
    }

    /**
     * Get stipend types
     *
     * <p>Returns all available scholarship/stipend types</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "stipends": [
     *     {
     *       "code": "11",
     *       "name": "Prezident stipendiyasi",
     *       "name_latin": "Presidential Scholarship",
     *       "amount": 1000000
     *     },
     *     {
     *       "code": "12",
     *       "name": "Akademik stipendiya",
     *       "name_latin": "Academic Scholarship",
     *       "amount": 500000
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @return list of stipend types with amounts
     */
    @GetMapping("/stipend")
    public ResponseEntity<Map<String, Object>> stipend() {
        log.info("💰 Classifier stipend - Fetching stipend types");
        Map<String, Object> result = classifiersCubaService.stipend();
        log.info("✅ Classifier stipend completed");
        return ResponseEntity.ok(result);
    }

    /**
     * Get stipend information (detailed catalog)
     *
     * <p>Returns detailed stipend catalog with eligibility criteria</p>
     *
     * @return detailed stipend information
     */
    @GetMapping("/stipendInfo")
    public ResponseEntity<Map<String, Object>> stipendInfo() {
        log.info("💰 Classifier stipendInfo - Fetching detailed info");
        Map<String, Object> result = classifiersCubaService.stipendInfo();
        log.info("✅ Classifier stipendInfo completed");
        return ResponseEntity.ok(result);
    }

    /**
     * Get hokimiyat (local government) data
     *
     * <p>Returns registry of local government offices and administrative divisions</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "hokimiyat": [
     *     {
     *       "code": "01",
     *       "region": "Toshkent shahri",
     *       "districts": [
     *         {"code": "0101", "name": "Bektemir tumani"},
     *         {"code": "0102", "name": "Chilonzor tumani"},
     *         ...
     *       ]
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @return hokimiyat registry with regions and districts
     */
    @GetMapping("/hokimiyat")
    public ResponseEntity<Map<String, Object>> hokimiyat() {
        log.info("🏛️ Classifier hokimiyat - Fetching registry");
        Map<String, Object> result = classifiersCubaService.hokimiyat();
        log.info("✅ Classifier hokimiyat completed");
        return ResponseEntity.ok(result);
    }

    /**
     * Get hokimiyat information (metadata)
     *
     * <p>Returns metadata about hokimiyat registries</p>
     *
     * @return hokimiyat metadata
     */
    @GetMapping("/hokimiyatInfo")
    public ResponseEntity<Map<String, Object>> hokimiyatInfo() {
        log.info("ℹ️ Classifier hokimiyatInfo - Fetching metadata");
        Map<String, Object> result = classifiersCubaService.hokimiyatInfo();
        log.info("✅ Classifier hokimiyatInfo completed");
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // ✅ 7 CLASSIFIER ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Separate controller per feature
    // Equivalent to: ClassifiersServiceBean.java in CUBA Platform
    // =====================================================
}
