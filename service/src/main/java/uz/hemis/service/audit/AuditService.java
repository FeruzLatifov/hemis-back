package uz.hemis.service.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;

import java.util.*;

/**
 * Audit loglarni o'qish va statistika xizmati.
 * Audit REPLICA DB dan JdbcTemplate orqali o'qiydi.
 *
 * <p>Yozish (INSERT) AuditRepository orqali master ga boradi.
 * Bu service faqat READ operatsiyalar — replica dan o'qiydi.</p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditService {

    private final JdbcTemplate jdbcTemplate;

    public AuditService(@Qualifier("auditReadJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =====================================================
    // Activity Logs
    // =====================================================

    public PageResponse<Map<String, Object>> getActivities(Map<String, String> filters, int page, int size) {
        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();
        applyCommonFilters(where, params, filters);
        applyFilter(where, params, filters, "action", "action");
        applyFilter(where, params, filters, "entityType", "entity_type");
        applyLikeFilter(where, params, filters, "search", "entity_name", "description");

        return queryPage("activity_log", where.toString(), params, page, size,
                "id, user_id, username, full_name, user_ip, action, entity_type, entity_id, entity_name, " +
                "changed_fields, endpoint, request_id, created_at");
    }

    public Map<String, Object> getActivityDetail(String id) {
        return queryById("activity_log", id,
                "id, user_id, username, full_name, user_ip, user_agent, action, entity_type, " +
                "entity_id, entity_name, old_value, new_value, changed_fields, request_id, " +
                "endpoint, description, created_at");
    }

    public PageResponse<Map<String, Object>> getEntityHistory(
            String entityType, String entityId, int page, int size) {
        String where = "WHERE entity_type = ? AND entity_id = ?";
        List<Object> params = new ArrayList<>(List.of(entityType, entityId));
        return queryPage("activity_log", where, params, page, size,
                "id, user_id, username, user_ip, action, entity_type, entity_id, entity_name, " +
                "old_value, new_value, changed_fields, request_id, description, created_at");
    }

    // =====================================================
    // Error Logs
    // =====================================================

    public PageResponse<Map<String, Object>> getErrors(Map<String, String> filters, int page, int size) {
        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();
        applyCommonFilters(where, params, filters);
        applyLikeFilter(where, params, filters, "errorType", "error_type");
        applyLikeFilter(where, params, filters, "search", "error_message");

        return queryPage("error_log", where.toString(), params, page, size,
                "id, user_id, username, user_ip, error_type, error_message, endpoint, " +
                "request_id, created_at");
    }

    public Map<String, Object> getErrorDetail(String id) {
        return queryById("error_log", id,
                "id, user_id, username, user_ip, error_type, error_message, stack_trace, " +
                "endpoint, request_id, request_body, created_at");
    }

    // =====================================================
    // Login Logs
    // =====================================================

    public PageResponse<Map<String, Object>> getLogins(Map<String, String> filters, int page, int size) {
        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();
        applyCommonFilters(where, params, filters);
        applyFilter(where, params, filters, "eventType", "event_type");

        return queryPage("login_log", where.toString(), params, page, size,
                "id, user_id, username, user_ip, user_agent, event_type, failure_reason, created_at");
    }

    // =====================================================
    // Statistics
    // =====================================================

    public Map<String, Object> getStats(String dateFrom, String dateTo) {
        Map<String, Object> stats = new LinkedHashMap<>();

        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (dateFrom != null && !dateFrom.isBlank()) {
            where.append(" AND created_at >= ?::timestamptz");
            params.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isBlank()) {
            where.append(" AND created_at <= ?::timestamptz");
            params.add(dateTo);
        }

        String whereStr = where.toString();

        // Total counts
        stats.put("totalActivities", countTable("activity_log", whereStr, params));
        stats.put("totalErrors", countTable("error_log", whereStr, params));
        stats.put("totalLogins", countTable("login_log", whereStr, params));

        // Top users (by activity)
        List<Object> topUserParams = new ArrayList<>(params);
        stats.put("topUsers", jdbcTemplate.queryForList(
                "SELECT username, COUNT(*) as count FROM activity_log " +
                whereStr + " AND username IS NOT NULL" +
                " GROUP BY username ORDER BY count DESC LIMIT 10",
                topUserParams.toArray()));

        // Error rates
        stats.put("errorsByType", jdbcTemplate.queryForList(
                "SELECT error_type, COUNT(*) as count FROM error_log " + whereStr +
                " GROUP BY error_type ORDER BY count DESC LIMIT 10",
                params.toArray()));

        // Login stats
        stats.put("loginsByType", jdbcTemplate.queryForList(
                "SELECT event_type, COUNT(*) as count FROM login_log " + whereStr +
                " GROUP BY event_type ORDER BY count DESC",
                params.toArray()));

        return stats;
    }

    // =====================================================
    // Private Helpers
    // =====================================================

    private PageResponse<Map<String, Object>> queryPage(String table, String where,
                                                         List<Object> params, int page, int size,
                                                         String columns) {
        // Count
        String countSql = "SELECT COUNT(*) FROM " + table + " " + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        // Data
        String dataSql = "SELECT " + columns + " FROM " + table + " " + where +
                " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(size);
        dataParams.add(page * size);

        List<Map<String, Object>> content = jdbcTemplate.queryForList(dataSql, dataParams.toArray());

        // Camel case conversion
        content = content.stream().map(this::toCamelCaseKeys).toList();

        long totalElements = total != null ? total : 0L;
        org.springframework.data.domain.Page<Map<String, Object>> springPage =
                new org.springframework.data.domain.PageImpl<>(
                        content,
                        org.springframework.data.domain.PageRequest.of(page, size),
                        totalElements);
        return PageResponses.from(springPage);
    }

    private Map<String, Object> queryById(String table, String id, String columns) {
        String sql = "SELECT " + columns + " FROM " + table + " WHERE id = ?::uuid";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, id);
        if (results.isEmpty()) return null;
        return toCamelCaseKeys(results.getFirst());
    }

    private void applyCommonFilters(StringBuilder where, List<Object> params, Map<String, String> filters) {
        if (filters.containsKey("userId") && !filters.get("userId").isBlank()) {
            where.append(" AND user_id = ?::uuid");
            params.add(filters.get("userId"));
        }
        if (filters.containsKey("username") && !filters.get("username").isBlank()) {
            where.append(" AND username ILIKE ?");
            params.add("%" + filters.get("username") + "%");
        }
        if (filters.containsKey("ip") && !filters.get("ip").isBlank()) {
            where.append(" AND user_ip = ?");
            params.add(filters.get("ip"));
        }
        if (filters.containsKey("dateFrom") && !filters.get("dateFrom").isBlank()) {
            where.append(" AND created_at >= ?::timestamptz");
            params.add(filters.get("dateFrom"));
        }
        if (filters.containsKey("dateTo") && !filters.get("dateTo").isBlank()) {
            where.append(" AND created_at <= ?::timestamptz");
            params.add(filters.get("dateTo"));
        }
    }

    private void applyFilter(StringBuilder where, List<Object> params, Map<String, String> filters,
                              String filterKey, String column) {
        if (filters.containsKey(filterKey) && !filters.get(filterKey).isBlank()) {
            where.append(" AND ").append(column).append(" = ?");
            params.add(filters.get(filterKey));
        }
    }

    private void applyLikeFilter(StringBuilder where, List<Object> params, Map<String, String> filters,
                                   String filterKey, String... columns) {
        if (filters.containsKey(filterKey) && !filters.get(filterKey).isBlank()) {
            String val = "%" + filters.get(filterKey) + "%";
            StringBuilder or = new StringBuilder(" AND (");
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) or.append(" OR ");
                or.append(columns[i]).append(" ILIKE ?");
                params.add(val);
            }
            or.append(")");
            where.append(or);
        }
    }

    private Long countTable(String table, String where, List<Object> params) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " " + where, Long.class, params.toArray());
    }

    private Map<String, Object> toCamelCaseKeys(Map<String, Object> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(snakeToCamel(entry.getKey()), unwrapJdbcValue(entry.getValue()));
        }
        return result;
    }

    /**
     * JDBC qaytargan maxsus tiplar (PgArray) Jackson tomonidan serialize qilib bo'lmaydi —
     * ular ichidan Connection ga reference yuradi. Shuning uchun ularni oddiy Java tiplarga
     * o'tkazamiz (String[]/List).
     */
    private Object unwrapJdbcValue(Object value) {
        if (value instanceof java.sql.Array array) {
            try {
                Object inner = array.getArray();
                if (inner instanceof Object[] arr) {
                    return Arrays.asList(arr);
                }
                return inner;
            } catch (java.sql.SQLException e) {
                log.warn("Failed to unwrap SQL Array: {}", e.getMessage());
                return null;
            }
        }
        return value;
    }

    private String snakeToCamel(String snake) {
        if (snake == null || !snake.contains("_")) return snake;
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }
}
