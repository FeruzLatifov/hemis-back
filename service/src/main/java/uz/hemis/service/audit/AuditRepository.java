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

import java.sql.Timestamp;
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
        try {
            AuditContext ctx = event.getContext();
            jdbcTemplate.update("""
                INSERT INTO activity_log (user_id, username, user_ip, user_agent, session_id,
                    action, entity_type, entity_id, entity_name, old_value, new_value,
                    changed_fields, request_id, endpoint, description, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::text[], ?, ?, ?, ?)
                """,
                    ctx != null ? ctx.getUserId() : null,
                    ctx != null ? ctx.getUsername() : null,
                    ctx != null ? ctx.getIp() : null,
                    ctx != null ? ctx.getUserAgent() : null,
                    ctx != null ? ctx.getSessionId() : null,
                    event.getAction().name(),
                    event.getEntityType(),
                    event.getEntityId(),
                    event.getEntityName(),
                    toJson(event.getOldValue()),
                    toJson(event.getNewValue()),
                    toTextArray(event.getChangedFields()),
                    ctx != null ? ctx.getRequestId() : null,
                    ctx != null ? ctx.getEndpoint() : null,
                    event.getDescription(),
                    Timestamp.from(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
            );
        } catch (Exception e) {
            log.error("Failed to save activity log: {}", e.getMessage());
        }
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

    private String toTextArray(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return "{" + String.join(",", list.stream()
                .map(s -> "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .toList()) + "}";
    }
}
