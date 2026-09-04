package uz.hemis.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.hemis.common.audit.*;
import uz.hemis.common.util.PiiMask;
import uz.hemis.common.vo.Pinfl;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Audit DB ga yozish uchun repository.
 * JdbcTemplate orqali alohida audit bazasiga insert qiladi.
 */
@Slf4j
@Repository
// Mirrors AuditDataSourceConfig, which is @Profile("!test") + the same property: the audit
// JdbcTemplate simply does not exist under the test profile. Without this the two conditions
// disagree — this bean loads, its datasource does not, and every @SpringBootTest dies on
// "No qualifying bean of type JdbcTemplate" whenever AUDIT_ENABLED=true is in the environment.
@Profile("!test")
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Sensitive field-name keys, NORMALIZED (lowercased, non-alphanumerics stripped) so that
     * {@code first_name}, {@code firstName} and {@code FirstName} all match one entry.
     * Bound as a {@link List} (not a bare {@link String}) so the YAML sequence in
     * {@code hemis.audit.redact-fields} actually resolves.
     */
    private final Set<String> redactKeys;

    /** Fallback when the property is absent — the same set the @Value default used to carry. */
    private static final List<String> DEFAULT_REDACT_FIELDS = List.of(
            "password", "confirmPassword", "newPassword", "oldPassword", "currentPassword",
            "token", "secret", "clientSecret", "client_secret", "plainSecret", "plain_secret",
            "authorization", "pinfl");

    @Autowired
    public AuditRepository(@Qualifier("auditJdbcTemplate") JdbcTemplate jdbcTemplate,
                           ObjectMapper objectMapper,
                           Environment environment) {
        this(jdbcTemplate, objectMapper, bindRedactFields(environment));
    }

    /** Explicit key list — used by tests, which have no Environment to bind from. */
    AuditRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, List<String> redactFields) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.redactKeys = redactFields.stream()
                .map(AuditRepository::normalizeKey)
                .filter(k -> !k.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * The configured key list, read the way {@code @ConfigurationProperties} would.
     *
     * <p>Not {@code @Value}: a YAML sequence is not a comma-separated string, so
     * {@code @Value("${hemis.audit.redact-fields:…}") List<String>} silently fell back to its own
     * default and every key the operator had added in application.yml (passport, FIO, phone, email,
     * address …) was written into old_value/new_value in clear text — readable by anyone holding
     * audit.view. Binder reads the sequence, and still accepts a comma-separated env override.</p>
     */
    private static List<String> bindRedactFields(Environment environment) {
        return Binder.get(environment)
                .bind("hemis.audit.redact-fields", Bindable.listOf(String.class))
                .orElse(DEFAULT_REDACT_FIELDS);
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
                        action, entity_type, entity_id, entity_name, scope_key, old_value, new_value,
                        changed_fields, request_id, endpoint, description, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?)
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
                // The owner scope (OTM code) — an indexed column, so "this OTM's history" is an
                // equality lookup and stays cheap when the row it describes is long gone.
                ps.setString(10, event.getScopeKey());
                ps.setString(11, oldJson);
                ps.setString(12, newJson);
                if (changedFields == null || changedFields.isEmpty()) {
                    ps.setNull(13, Types.ARRAY);
                } else {
                    Array array = con.createArrayOf("text", changedFields.toArray(new String[0]));
                    ps.setArray(13, array);
                }
                ps.setString(14, ctx != null ? ctx.getRequestId() : null);
                ps.setString(15, ctx != null ? ctx.getEndpoint() : null);
                ps.setString(16, event.getDescription());
                ps.setTimestamp(17, createdAt);
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
                    // Same masking as the JSONB snapshots: an exception message and a stack trace
                    // routinely quote the offending value (a PINFL in a constraint violation, a
                    // passport number in a validation error), and /audit/errors/{id} serves them.
                    maskEmbeddedPinfl(event.getErrorMessage()),
                    maskEmbeddedPinfl(event.getStackTrace()),
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

    /**
     * PII/secret redaction — RECURSIVE over Map/List/scalar. Two independent defenses:
     * <ol>
     *   <li><b>Key-based:</b> any field whose (normalized) name is in {@link #redactKeys} is masked
     *       by inferred type — pinfl → {@link Pinfl#maskOrEmpty}, phone/email/name → {@link PiiMask},
     *       everything else → prefix mask.</li>
     *   <li><b>Value-based safety net:</b> any string containing a bare 14-digit run (a PINFL) is
     *       masked wherever it appears — even under an unexpected key or embedded in free text
     *       (e.g. a captured {@code error_log.request_body} {@code _raw} fragment).</li>
     * </ol>
     * This is the single choke-point for both {@code activity_log.old_value/new_value} and
     * {@code error_log.request_body}, both of which flow through {@link #toJson}.
     */
    // Package-private (not private) so AuditRedactionTest can assert the PII/PINFL guardrail directly.
    Map<String, Object> redactSensitiveFields(Map<String, Object> map) {
        if (map == null) return null;
        @SuppressWarnings("unchecked")
        Map<String, Object> redacted = (Map<String, Object>) redactValue(map);
        return redacted;
    }

    private Object redactValue(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case Map<?, ?> m -> {
                Map<String, Object> out = new LinkedHashMap<>();
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    String key = String.valueOf(e.getKey());
                    out.put(key, isSensitiveKey(key) ? maskField(key, e.getValue()) : redactValue(e.getValue()));
                }
                return out;
            }
            case List<?> list -> {
                List<Object> out = new ArrayList<>(list.size());
                for (Object item : list) out.add(redactValue(item));
                return out;
            }
            case String s -> {
                return maskEmbeddedPinfl(s);
            }
            case Number n -> {
                // Value-net for a bare 14-digit PINFL delivered as a numeric literal under any key.
                String digits = n.toString();
                return PINFL_RUN.matcher(digits).matches() ? Pinfl.maskOrEmpty(digits) : value;
            }
            default -> {
                return value;
            }
        }
    }

    private boolean isSensitiveKey(String key) {
        return redactKeys.contains(normalizeKey(key));
    }

    /** Mask a value under a sensitive key by inferred type; nested structures are still walked. */
    private Object maskField(String key, Object value) {
        if (value == null) return null;
        // A sensitive field may arrive as a numeric literal (e.g. {"pinfl": 12345678901234}) —
        // mask its digit form too. Map/List under a sensitive key keeps recursing via redactValue.
        String s = (value instanceof String str) ? str
                : (value instanceof Number) ? value.toString()
                : null;
        if (s == null) return redactValue(value);
        String nk = normalizeKey(key);
        if (nk.equals("pinfl") || nk.equals("jshshir")) return Pinfl.maskOrEmpty(s);
        if (nk.contains("phone")) return PiiMask.phone(s);
        if (nk.contains("email")) return PiiMask.email(s);
        if (nk.contains("name")) return PiiMask.name(s);
        return s.length() > 4 ? s.substring(0, 4) + "****" : "****";
    }

    /** A bare 14-digit PINFL run, not part of a longer number. */
    private static final Pattern PINFL_RUN = Pattern.compile("(?<!\\d)\\d{14}(?!\\d)");

    private static String maskEmbeddedPinfl(String s) {
        if (s == null || s.length() < 14) return s;
        Matcher m = PINFL_RUN.matcher(s);
        if (!m.find()) return s;
        return m.replaceAll(mr -> Matcher.quoteReplacement(Pinfl.maskOrEmpty(mr.group())));
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

}
