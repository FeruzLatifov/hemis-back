package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Stipend Classifier Service - stipend-specific classifier operations.
 *
 * <p>Handles OLD-HEMIS stipend endpoints:</p>
 * <ul>
 *   <li>GET /app/rest/v2/services/classifiers/stipend</li>
 *   <li>GET /app/rest/v2/services/classifiers/stipendInfo</li>
 * </ul>
 *
 * <p>Extracted from {@link ClassifierLegacyService} to reduce class size.
 * Delegates to {@link HokimiyatClassifierService} for shared JDBC helpers
 * (classifier item loading, university classifier, info queries).</p>
 *
 * @since 2.2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StipendClassifierService {

    private final HokimiyatClassifierService hokimiyatClassifierService;

    /**
     * OLD-HEMIS stipend classifiers - exact mapping (13 ta)
     * Key: API response dagi nom
     * Value: Database table nomi
     */
    private static final Map<String, String> STIPEND_CLASSIFIER_MAP = new LinkedHashMap<>() {{
        put("h_soato", "hemishe_h_soato");
        put("h_education_type", "hemishe_h_education_type");
        put("h_education_form", "hemishe_h_education_form");
        put("h_education_year", "hemishe_h_education_year");
        put("h_student_success", "hemishe_h_student_achievement_type");
        put("h_university", "hemishe_e_university"); // Special: EUniversity table
        put("h_ownership", "hemishe_h_ownership");
        put("h_course", "hemishe_h_course");
        put("h_nationality", "hemishe_h_nationality");
        put("h_bachelor_speciality", "hemishe_h_speciality_bachelor");
        put("h_master_speciality", "hemishe_h_speciality_master");
        put("h_citizenship_type", "hemishe_h_citizenship");
        put("h_social_category", "hemishe_h_student_social_type");
    }};

    /**
     * Get stipend classifiers with items
     * OLD-HEMIS /stipend endpoint (13 ta classifier)
     */
    public Map<String, Object> getStipendClassifiers() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : STIPEND_CLASSIFIER_MAP.entrySet()) {
            String apiKey = entry.getKey();
            String tableName = entry.getValue();

            try {
                if ("h_university".equals(apiKey)) {
                    Map<String, Object> uniData = hokimiyatClassifierService.getUniversityClassifierForHokimiyat();
                    if (uniData != null) {
                        classifiersList.add(uniData);
                    }
                    continue;
                }

                Map<String, Object> classifierData = hokimiyatClassifierService.getClassifierWithItemsForHokimiyat(apiKey, tableName);
                if (classifierData != null) {
                    classifiersList.add(classifierData);
                }
            } catch (Exception e) {
                log.debug("Error loading stipend classifier {}: {}", apiKey, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    /**
     * Get stipend classifiers info (metadata only, no items)
     * OLD-HEMIS /stipendInfo endpoint
     */
    public Map<String, Object> getStipendClassifiersInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : STIPEND_CLASSIFIER_MAP.entrySet()) {
            String apiKey = entry.getKey();
            String tableName = entry.getValue();

            try {
                if ("h_university".equals(apiKey)) {
                    classifiersList.add(hokimiyatClassifierService.getUniversityClassifierInfoCompat());
                    continue;
                }

                Map<String, Object> classifierInfo = hokimiyatClassifierService.getClassifierInfoForGroup(apiKey, tableName);
                if (classifierInfo != null) {
                    classifiersList.add(classifierInfo);
                }
            } catch (Exception e) {
                log.debug("Error loading stipendInfo classifier {}: {}", apiKey, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }
}
