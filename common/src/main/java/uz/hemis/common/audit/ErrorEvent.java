package uz.hemis.common.audit;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Xato audit event — error_log jadvaliga yoziladi.
 */
@Data
@Builder
public class ErrorEvent {
    private AuditContext context;
    private String errorType;
    private String errorMessage;
    private String stackTrace;
    private String endpoint;
    private Map<String, Object> requestBody;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
