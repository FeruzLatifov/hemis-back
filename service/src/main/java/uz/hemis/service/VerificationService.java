package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Verification;
import uz.hemis.domain.repository.VerificationRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Verification Service - DTM verification ballari
 *
 * <p>OLD-HEMIS Compatible - /student/verify endpoint uchun</p>
 *
 * <p>Response format:</p>
 * <pre>
 * {
 *   "success": true,
 *   "count": 2,
 *   "records": [
 *     {
 *       "_entityName": "hemishe_EVerification",
 *       "id": "...",
 *       "pinfl": "...",
 *       "points": "69.3",
 *       "paymentForm": {...},
 *       "educationType": {...},
 *       "university": {...},
 *       "educationYear": {...},
 *       "category": {...}
 *     }
 *   ]
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Verify student by PINFL - OLD-HEMIS compatible
     *
     * <p>Returns verification records (DTM ballari) for given PINFL</p>
     *
     * @param pinfl Personal identification number
     * @return OLD-HEMIS formatted response with verification records
     */
    public Map<String, Object> verifyByPinfl(String pinfl) {
        log.info("Verifying student by PINFL: {}", pinfl);
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            List<Verification> verifications = verificationRepository.findByPinfl(pinfl);

            if (verifications.isEmpty()) {
                // Return empty result (OLD-HEMIS compatible)
                result.put("success", true);
                result.put("count", 0);
                result.put("records", new ArrayList<>());
                return result;
            }

            // Convert to OLD-HEMIS CUBA format
            List<Map<String, Object>> records = new ArrayList<>();
            for (Verification v : verifications) {
                records.add(toLegacyMap(v));
            }

            result.put("success", true);
            result.put("count", records.size());
            result.put("records", records);

            return result;

        } catch (Exception e) {
            log.error("Error verifying student: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Convert Verification entity to OLD-HEMIS CUBA format
     */
    private Map<String, Object> toLegacyMap(Verification v) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", "hemishe_EVerification");
        map.put("id", v.getId().toString());
        map.put("pinfl", v.getPinfl());
        map.put("version", v.getVersion());
        map.put("points", v.getPoints());

        // Nested references
        map.put("paymentForm", loadSimpleReference("hemishe_h_payment_form", "hemishe_HPaymentForm", v.getPaymentForm()));
        map.put("educationType", loadSimpleReference("hemishe_h_education_type", "hemishe_HEducationType", v.getEducationType()));
        map.put("university", loadUniversity(v.getUniversity()));
        map.put("educationYear", loadSimpleReference("hemishe_h_education_year", "hemishe_HEducationYear", v.getEducationYear()));
        map.put("category", loadVerificationType(v.getCategory()));

        return map;
    }

    /**
     * Load simple classifier reference from CUBA tables
     */
    private Map<String, Object> loadSimpleReference(String tableName, String entityName, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name, name_ru, name_en, active, version FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("_entityName", entityName);
            ref.put("id", code);
            ref.put("code", code);
            ref.put("name", row.get("name"));
            ref.put("nameRu", row.get("name_ru"));
            ref.put("nameEn", row.get("name_en"));
            ref.put("active", row.get("active"));
            ref.put("version", row.get("version"));

            return ref;
        } catch (Exception e) {
            log.debug("Failed to load reference {}.{}: {}", tableName, code, e.getMessage());
            return null;
        }
    }

    /**
     * Load university from hemishe_e_university
     */
    private Map<String, Object> loadUniversity(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = """
                SELECT code, name, student_url, teacher_url, tin, address, active,
                       add_student, allow_grouping, allow_transfer_outside,
                       accreditation_edit, gpa_edit, version
                FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL
                """;
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            Map<String, Object> uni = new LinkedHashMap<>();
            uni.put("_entityName", "hemishe_EUniversity");
            uni.put("id", code);
            uni.put("code", code);
            uni.put("name", row.get("name"));
            uni.put("studentUrl", row.get("student_url"));
            uni.put("teacherUrl", row.get("teacher_url"));
            uni.put("tin", row.get("tin"));
            uni.put("address", row.get("address"));
            uni.put("active", row.get("active"));
            uni.put("addStudent", row.get("add_student"));
            uni.put("allowGrouping", row.get("allow_grouping"));
            uni.put("allowTransferOutside", row.get("allow_transfer_outside"));
            uni.put("accreditationEdit", row.get("accreditation_edit"));
            uni.put("gpaEdit", row.get("gpa_edit"));
            uni.put("version", row.get("version"));

            return uni;
        } catch (Exception e) {
            log.debug("Failed to load university {}: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Load verification type from hemishe_h_verification_type
     */
    private Map<String, Object> loadVerificationType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name, name_ru, name_en, version FROM hemishe_h_verification_type WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("_entityName", "hemishe_HVerificationType");
            ref.put("id", code);
            ref.put("code", code);
            ref.put("name", row.get("name"));
            ref.put("nameRu", row.get("name_ru"));
            ref.put("nameEn", row.get("name_en"));
            ref.put("version", row.get("version"));

            return ref;
        } catch (Exception e) {
            log.debug("Failed to load verification type {}: {}", code, e.getMessage());
            return null;
        }
    }
}
