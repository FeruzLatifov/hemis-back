package uz.hemis.common.audit;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

/**
 * Xato audit event — error_log jadvaliga yoziladi.
 *
 * <p>Immutable: @Async orqali boshqa threadga o'tganda thread-safe.</p>
 */
@Value
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
