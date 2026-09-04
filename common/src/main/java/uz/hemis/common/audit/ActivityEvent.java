package uz.hemis.common.audit;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * CRUD operatsiya audit event — activity_log jadvaliga yoziladi.
 *
 * <p>Immutable: @Async orqali boshqa threadga o'tganda thread-safe.</p>
 */
@Value
@Builder
public class ActivityEvent {
    private AuditContext context;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String entityName;
    /**
     * Owner of the record (e.g. the OTM code) — the scope a history question is asked in.
     *
     * <p>A link row is hard-deleted, so "everything that happened to OTM 301's attachments" cannot be
     * answered from the row. An indexed column, not a naming convention: the query is an equality on
     * (entity_type, scope_key), which stays cheap as the log grows.</p>
     */
    private String scopeKey;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private List<String> changedFields;
    private String description;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
