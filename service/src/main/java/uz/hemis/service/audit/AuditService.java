package uz.hemis.service.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.PageResponse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Audit loglarni o'qish va statistika xizmati.
 * Alohida audit DB dan JdbcTemplate orqali o'qiydi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditService {

    @Qualifier("auditJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

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
                "id, user_id, username, user_ip, action, entity_type, entity_id, entity_name, " +
                "changed_fields, endpoint, request_id, created_at");
    }

    public Map<String, Object> getActivityDetail(String id) {
        return queryById("activity_log", id,
                "id, user_id, username, user_ip, user_agent, session_id, action, entity_type, " +
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

        String dateFilter = buildDateFilter(dateFrom, dateTo);

        // Total counts
        stats.put("totalActivities", countTable("activity_log", dateFilter));
        stats.put("totalErrors", countTable("error_log", dateFilter));
        stats.put("totalLogins", countTable("login_log", dateFilter));

        // Top users (by activity)
        stats.put("topUsers", jdbcTemplate.queryForList(
                "SELECT username, COUNT(*) as count FROM activity_log " +
                "WHERE username IS NOT NULL " + dateFilter +
                " GROUP BY username ORDER BY count DESC LIMIT 10"));

        // Error rates
        stats.put("errorsByType", jdbcTemplate.queryForList(
                "SELECT error_type, COUNT(*) as count FROM error_log WHERE 1=1 " + dateFilter +
                " GROUP BY error_type ORDER BY count DESC LIMIT 10"));

        // Login stats
        stats.put("loginsByType", jdbcTemplate.queryForList(
                "SELECT event_type, COUNT(*) as count FROM login_log WHERE 1=1 " + dateFilter +
                " GROUP BY event_type ORDER BY count DESC"));

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

        int totalPages = (int) Math.ceil((double) (total != null ? total : 0) / size);
        return PageResponse.<Map<String, Object>>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total != null ? total : 0)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .numberOfElements(content.size())
                .empty(content.isEmpty())
                .build();
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

    private Long countTable(String table, String dateFilter) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE 1=1 " + dateFilter, Long.class);
    }

    private String buildDateFilter(String dateFrom, String dateTo) {
        StringBuilder sb = new StringBuilder();
        if (dateFrom != null && !dateFrom.isBlank()) {
            sb.append(" AND created_at >= '").append(dateFrom.replace("'", "")).append("'::timestamptz");
        }
        if (dateTo != null && !dateTo.isBlank()) {
            sb.append(" AND created_at <= '").append(dateTo.replace("'", "")).append("'::timestamptz");
        }
        return sb.toString();
    }

    private Map<String, Object> toCamelCaseKeys(Map<String, Object> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(snakeToCamel(entry.getKey()), entry.getValue());
        }
        return result;
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
