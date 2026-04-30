package uz.hemis.common.audit;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Audit context — kim, qayerdan, qaysi so'rov.
 *
 * <p>Immutable: thread-safe, @Async xavfsiz.</p>
 */
@Value
@Builder
public class AuditContext {
    private UUID userId;
    private String username;
    private String fullName;
    private String ip;
    private String userAgent;
    private String requestId;
    private String endpoint;
}
