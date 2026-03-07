package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * GUVD Legacy Service — OLD-HEMIS GuvdServiceBean compatible.
 *
 * <p>Implements two methods matching old-hemis CUBA service:</p>
 * <ul>
 *   <li>classifiers() — 5 classifiers (h_soato with '1726%' condition, h_gender,
 *       h_university_form, h_ownership, h_university)</li>
 *   <li>objects() — All universities (code, name, tin)</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GuvdLegacyService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * GUVD classifiers — 5 classifiers matching old-hemis GuvdServiceBean.classifiers().
     * Uses SUM(version) like old-hemis.
     *
     * <p>Old-hemis map:</p>
     * <ul>
     *   <li>h_soato — hemishe_h_soato WHERE code LIKE '1726%'</li>
     *   <li>h_gender — hemishe_h_gender</li>
     *   <li>h_university_form — hemishe_h_university_type</li>
     *   <li>h_ownership — hemishe_h_ownership</li>
     *   <li>h_university — hemishe_e_university</li>
     * </ul>
     */
    public Map<String, Object> getClassifiers() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            List<Map<String, Object>> classifiersList = new ArrayList<>();

            // h_soato — with condition: code LIKE '1726%'
            classifiersList.add(buildClassifier("h_soato", "hemishe_h_soato",
                    "Viloyat va tumanlar", "code LIKE '1726%'"));

            // h_gender
            classifiersList.add(buildClassifier("h_gender", "hemishe_h_gender",
                    "Jins turlari", null));

            // h_university_form
            classifiersList.add(buildClassifier("h_university_form", "hemishe_h_university_type",
                    "Oliy ta'lim muassasasi tashkiliy shakllari", null));

            // h_ownership
            classifiersList.add(buildClassifier("h_ownership", "hemishe_h_ownership",
                    "OTM mulkchilik shakllari", null));

            // h_university
            classifiersList.add(buildUniversityClassifier());

            result.put("success", true);
            result.put("classifiers", classifiersList);
        } catch (Exception e) {
            log.error("Error loading GUVD classifiers: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Server error");
            result.put("exception", e.getMessage());
        }

        return result;
    }

    /**
     * GUVD objects — All universities (code, name, tin).
     * Matches old-hemis GuvdServiceBean.objects().
     * Format: { success: true, count: N, data: [{code, name, tin}, ...] }
     */
    public Map<String, Object> getObjects() {
        Map<String, Object> result = new LinkedHashMap<>();

        String sql = "SELECT code, name, tin FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY code";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("_entityName", "hemishe_EUniversity");
            item.put("_instanceName", row.get("code") + "-" + row.get("name"));
            item.put("id", row.get("code"));
            item.put("code", row.get("code"));
            item.put("name", row.get("name"));
            item.put("tin", row.get("tin"));
            item.values().removeIf(Objects::isNull);
            data.add(item);
        }

        result.put("success", true);
        result.put("count", data.size());
        result.put("data", data);
        return result;
    }

    /**
     * Build a single classifier with items, matching old-hemis GuvdServiceBean.classifier().
     */
    private Map<String, Object> buildClassifier(String apiName, String tableName,
                                                 String title, String condition) {
        // Build WHERE clause
        List<String> conditions = new ArrayList<>();
        if (columnExists(tableName, "delete_ts")) {
            conditions.add("delete_ts IS NULL");
        }
        if (condition != null) {
            conditions.add(condition);
        }
        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        // Version: SUM (old-hemis compatible)
        boolean hasVersion = columnExists(tableName, "version");
        String versionExpr = hasVersion ? "COALESCE(SUM(COALESCE(version, 1)), 0)" : "0";
        String statsSql = "SELECT COUNT(*) as cnt, " + versionExpr + " as ver FROM " + tableName + whereClause;
        Map<String, Object> stats = jdbcTemplate.queryForMap(statsSql);
        long count = ((Number) stats.get("cnt")).longValue();
        long versionSum = ((Number) stats.get("ver")).longValue();

        // Items
        StringBuilder itemsSql = new StringBuilder("SELECT code, name");
        if (columnExists(tableName, "active")) itemsSql.append(", active");
        if (hasVersion) itemsSql.append(", COALESCE(version, 1) as version");
        itemsSql.append(" FROM ").append(tableName).append(whereClause);
        itemsSql.append(" ORDER BY code");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(itemsSql.toString());

        String entityName = getCubaEntityName(tableName);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            if (entityName != null) {
                item.put("_entityName", entityName);
                item.put("_instanceName", row.get("code") + "-" + row.get("name"));
            }
            item.put("id", row.get("code"));
            item.put("code", row.get("code"));
            item.put("name", row.get("name"));
            if (row.containsKey("active")) item.put("active", row.get("active"));
            if (row.containsKey("version")) item.put("version", row.get("version"));
            item.values().removeIf(Objects::isNull);
            items.add(item);
        }

        Map<String, Object> classifierData = new LinkedHashMap<>();
        classifierData.put("title", title);
        classifierData.put("version", versionSum);
        classifierData.put("count", count);
        classifierData.put("items", items);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(apiName, classifierData);
        return wrapper;
    }

    /**
     * Build h_university classifier for GUVD (uses hUniversity-view equivalent).
     */
    private Map<String, Object> buildUniversityClassifier() {
        String statsSql = "SELECT COUNT(*) as cnt, COALESCE(SUM(COALESCE(version, 1)), 0) as ver " +
                           "FROM hemishe_e_university WHERE delete_ts IS NULL";
        Map<String, Object> stats = jdbcTemplate.queryForMap(statsSql);
        long count = ((Number) stats.get("cnt")).longValue();
        long versionSum = ((Number) stats.get("ver")).longValue();

        String sql = "SELECT code, name, active, COALESCE(version, 1) as version " +
                     "FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY code";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("_entityName", "hemishe_EUniversity");
            item.put("_instanceName", row.get("code") + "-" + row.get("name"));
            item.put("id", row.get("code"));
            item.put("code", row.get("code"));
            item.put("name", row.get("name"));
            item.put("active", row.get("active"));
            item.put("version", row.get("version"));
            item.values().removeIf(Objects::isNull);
            items.add(item);
        }

        Map<String, Object> classifierData = new LinkedHashMap<>();
        classifierData.put("title", "Oliy ta'lim muassasalari ro'yxati");
        classifierData.put("version", versionSum);
        classifierData.put("count", count);
        classifierData.put("items", items);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("h_university", classifierData);
        return wrapper;
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT EXISTS(SELECT 1 FROM information_schema.columns " +
                         "WHERE table_schema = 'public' AND table_name = ? AND column_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName, columnName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    private String getCubaEntityName(String tableName) {
        return switch (tableName) {
            case "hemishe_h_soato" -> "hemishe_HSoato";
            case "hemishe_h_gender" -> "hemishe_HGender";
            case "hemishe_h_university_type" -> "hemishe_HUniversityType";
            case "hemishe_h_ownership" -> "hemishe_HOwnership";
            case "hemishe_e_university" -> "hemishe_EUniversity";
            default -> null;
        };
    }
}
