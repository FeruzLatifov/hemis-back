package uz.hemis.common.audit;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Autentifikatsiya hodisa audit event — login_log jadvaliga yoziladi.
 */
@Data
@Builder
public class LoginEvent {
    private AuditContext context;
    private LoginEventType eventType;
    private String failureReason;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public enum LoginEventType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        LOGOUT,
        TOKEN_REFRESH,
        SESSION_EXPIRED
    }
}
