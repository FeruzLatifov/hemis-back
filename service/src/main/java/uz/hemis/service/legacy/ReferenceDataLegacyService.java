package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for loading reference data (university, educationYear, course, speciality)
 * from the database with simple in-memory caching.
 *
 * Extracted from multiple CUBA-compatible entity controllers that were using
 * JdbcTemplate directly to look up reference table data for building nested objects.
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferenceDataLegacyService {

    private final JdbcTemplate jdbcTemplate;

    private final Map<String, Map<String, String>> universityCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> educationYearCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> courseCache = new ConcurrentHashMap<>();

    /**
     * Get university data (code, name) from DB with caching.
     *
     * @param code university code
     * @return map with "code" and "name" keys
     */
    public Map<String, String> getUniversityData(String code) {
        if (universityCache.containsKey(code)) {
            return universityCache.get(code);
        }

        try {
            Map<String, String> data = jdbcTemplate.queryForObject(
                "SELECT code, name FROM hemishe_e_university WHERE code = ?",
                (rs, rowNum) -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("code", rs.getString("code"));
                    m.put("name", rs.getString("name"));
                    return m;
                },
                code
            );
            if (data != null) {
                universityCache.put(code, data);
                return data;
            }
        } catch (Exception e) {
            log.warn("University not found for code: {}", code);
        }

        Map<String, String> fallback = new LinkedHashMap<>();
        fallback.put("code", code);
        fallback.put("name", "University " + code);
        return fallback;
    }

    /**
     * Get education year data (code, name) from DB with caching.
     *
     * @param code education year code
     * @return map with "code" and "name" keys
     */
    public Map<String, String> getEducationYearData(String code) {
        if (educationYearCache.containsKey(code)) {
            return educationYearCache.get(code);
        }

        try {
            Map<String, String> data = jdbcTemplate.queryForObject(
                "SELECT code, name FROM hemishe_h_education_year WHERE code = ?",
                (rs, rowNum) -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("code", rs.getString("code"));
                    m.put("name", rs.getString("name"));
                    return m;
                },
                code
            );
            if (data != null) {
                educationYearCache.put(code, data);
                return data;
            }
        } catch (Exception e) {
            log.warn("Education year not found for code: {}", code);
        }

        Map<String, String> fallback = new LinkedHashMap<>();
        fallback.put("code", code);
        fallback.put("name", code);
        return fallback;
    }

    /**
     * Get course data (code, name) from DB with caching.
     *
     * @param code course code
     * @return map with "code" and "name" keys
     */
    public Map<String, String> getCourseData(String code) {
        if (courseCache.containsKey(code)) {
            return courseCache.get(code);
        }

        try {
            Map<String, String> data = jdbcTemplate.queryForObject(
                "SELECT code, name FROM hemishe_h_course WHERE code = ?",
                (rs, rowNum) -> {
                    Map<String, String> result = new LinkedHashMap<>();
                    result.put("code", rs.getString("code"));
                    result.put("name", rs.getString("name"));
                    return result;
                },
                code
            );
            if (data != null) {
                courseCache.put(code, data);
                return data;
            }
        } catch (Exception e) {
            log.warn("Course not found for code: {}", code);
        }

        Map<String, String> fallback = new LinkedHashMap<>();
        fallback.put("code", code);
        fallback.put("name", code + "-kurs");
        return fallback;
    }

    /**
     * Get speciality doctoral data (id, name) from DB.
     *
     * @param specialityId the speciality UUID
     * @return map with "id" and "name" keys, or null if not found
     */
    public Map<String, Object> getSpecialityDoctoralData(UUID specialityId) {
        try {
            String sql = "SELECT id, name FROM hemishe_h_speciality_doctoral WHERE id = ? AND delete_ts IS NULL";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, specialityId);

            if (!results.isEmpty()) {
                return results.get(0);
            }
        } catch (Exception e) {
            log.warn("SpecialityDoctoral {} ni olishda xatolik: {}", specialityId, e.getMessage());
        }
        return null;
    }

    /**
     * Check if a classifier code is valid (exists and not soft-deleted).
     *
     * @param tableName the classifier table name (must be alphanumeric + underscore)
     * @param code the classifier code
     * @return true if valid
     */
    public boolean isValidClassifier(String tableName, String code) {
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            log.warn("Invalid table name: {}", tableName);
            return false;
        }
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code);
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("Error checking classifier {}/{}: {}", tableName, code, e.getMessage());
            return false;
        }
    }
}
