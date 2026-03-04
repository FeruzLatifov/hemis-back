package uz.hemis.common.audit;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Autentifikatsiya hodisa audit event — login_log jadvaliga yoziladi.
 *
 * <p>Immutable: @Async orqali boshqa threadga o'tganda thread-safe.</p>
 */
@Value
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
