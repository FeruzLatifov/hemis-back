package uz.hemis.common.audit;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * CRUD operatsiya audit event — activity_log jadvaliga yoziladi.
 */
@Data
@Builder
public class ActivityEvent {
    private AuditContext context;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String entityName;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private List<String> changedFields;
    private String description;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
