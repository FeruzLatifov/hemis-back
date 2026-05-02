package uz.hemis.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.hemis.common.audit.*;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Audit DB ga yozish uchun repository.
 * JdbcTemplate orqali alohida audit bazasiga insert qiladi.
 */
@Slf4j
@Repository
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hemis.audit.redact-fields:password}")
    private String redactFieldsConfig;

    public AuditRepository(@Qualifier("auditJdbcTemplate") JdbcTemplate jdbcTemplate,
                           ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void saveActivity(ActivityEvent event) {
        AuditContext ctx = event.getContext();
        String oldJson = toJson(event.getOldValue());
        String newJson = toJson(event.getNewValue());
        List<String> changedFields = event.getChangedFields();
        Timestamp createdAt = Timestamp.from(event.getTimestamp() != null ? event.getTimestamp() : Instant.now());

        try {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO activity_log (user_id, username, full_name, user_ip, user_agent,
                        action, entity_type, entity_id, entity_name, old_value, new_value,
                        changed_fields, request_id, endpoint, description, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?)
                    """);
                setObjectOrNull(ps, 1, ctx != null ? ctx.getUserId() : null, Types.OTHER);
                ps.setString(2, ctx != null ? ctx.getUsername() : null);
                ps.setString(3, ctx != null ? ctx.getFullName() : null);
                ps.setString(4, ctx != null ? ctx.getIp() : null);
                ps.setString(5, ctx != null ? ctx.getUserAgent() : null);
                ps.setString(6, event.getAction().name());
                ps.setString(7, event.getEntityType());
                ps.setString(8, event.getEntityId());
                ps.setString(9, event.getEntityName());
                ps.setString(10, oldJson);
                ps.setString(11, newJson);
                if (changedFields == null || changedFields.isEmpty()) {
                    ps.setNull(12, Types.ARRAY);
                } else {
                    Array array = con.createArrayOf("text", changedFields.toArray(new String[0]));
                    ps.setArray(12, array);
                }
                ps.setString(13, ctx != null ? ctx.getRequestId() : null);
                ps.setString(14, ctx != null ? ctx.getEndpoint() : null);
                ps.setString(15, event.getDescription());
                ps.setTimestamp(16, createdAt);
                return ps;
            });
        } catch (Exception e) {
            log.error("Failed to save activity log: {}", e.getMessage());
        }
    }

    private static void setObjectOrNull(PreparedStatement ps, int index, Object value, int sqlType) throws SQLException {
        if (value == null) ps.setNull(index, sqlType);
        else ps.setObject(index, value, sqlType);
    }

    public void saveError(ErrorEvent event) {
        try {
            AuditContext ctx = event.getContext();
            jdbcTemplate.update("""
                INSERT INTO error_log (user_id, username, user_ip, error_type, error_message,
                    stack_trace, endpoint, request_id, request_body, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)
                """,
                    ctx != null ? ctx.getUserId() : null,
                    ctx != null ? ctx.getUsername() : null,
                    ctx != null ? ctx.getIp() : null,
                    event.getErrorType(),
                    event.getErrorMessage(),
                    event.getStackTrace(),
                    event.getEndpoint(),
                    ctx != null ? ctx.getRequestId() : null,
                    toJson(event.getRequestBody()),
                    Timestamp.from(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
            );
        } catch (Exception e) {
            log.error("Failed to save error log: {}", e.getMessage());
        }
    }

    public void saveLogin(LoginEvent event) {
        try {
            AuditContext ctx = event.getContext();
            jdbcTemplate.update("""
                INSERT INTO login_log (user_id, username, user_ip, user_agent,
                    event_type, failure_reason, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                    ctx != null ? ctx.getUserId() : null,
                    ctx != null ? ctx.getUsername() : "unknown",
                    ctx != null ? ctx.getIp() : null,
                    ctx != null ? ctx.getUserAgent() : null,
                    event.getEventType().name(),
                    event.getFailureReason(),
                    Timestamp.from(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
            );
        } catch (Exception e) {
            log.error("Failed to save login log: {}", e.getMessage());
        }
    }

    /** JSONB snapshot hajmini cheklash — audit DB bloat oldini olish. */
    private static final int MAX_JSON_BYTES = 100_000;

    private String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return null;
        try {
            String json = objectMapper.writeValueAsString(redactSensitiveFields(map));
            if (json.length() > MAX_JSON_BYTES) {
                log.warn("Audit JSON truncated from {} to {} chars", json.length(), MAX_JSON_BYTES);
                json = json.substring(0, MAX_JSON_BYTES - 20) + "\"...truncated\"}";
            }
            return json;
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize to JSON: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> redactSensitiveFields(Map<String, Object> map) {
        if (map == null || redactFieldsConfig == null || redactFieldsConfig.isBlank()) return map;
        List<String> redactFields = List.of(redactFieldsConfig.split(","));
        Map<String, Object> result = new LinkedHashMap<>(map);
        for (String field : redactFields) {
            String key = field.trim();
            if (result.containsKey(key) && result.get(key) != null) {
                String val = result.get(key).toString();
                result.put(key, val.length() > 4 ? val.substring(0, 4) + "****" : "****");
            }
        }
        return result;
    }

}
