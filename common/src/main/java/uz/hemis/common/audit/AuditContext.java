package uz.hemis.common.audit;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Audit context — kim, qayerdan, qaysi so'rov.
 */
@Data
@Builder
public class AuditContext {
    private UUID userId;
    private String username;
    private String ip;
    private String userAgent;
    private String sessionId;
    private String requestId;
    private String endpoint;
}
