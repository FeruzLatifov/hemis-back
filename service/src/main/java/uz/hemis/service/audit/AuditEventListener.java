package uz.hemis.service.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uz.hemis.common.audit.*;

/**
 * Audit Event Listener — barcha audit eventlarni ushlab, DB ga yozadi.
 *
 * <p>@Async — asosiy so'rov threadni bloklamaydi.</p>
 * <p>@EventListener — Spring ApplicationEvent orqali yetkaziladi.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
// Mirrors AuditDataSourceConfig, which is @Profile("!test") + the same property: the audit
// JdbcTemplate simply does not exist under the test profile. Without this the two conditions
// disagree — this bean loads, its datasource does not, and every @SpringBootTest dies on
// "No qualifying bean of type JdbcTemplate" whenever AUDIT_ENABLED=true is in the environment.
@Profile("!test")
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditEventListener {

    private final AuditRepository auditRepository;

    @Async("auditTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleActivity(ActivityEvent event) {
        log.debug("Audit activity: {} {} {}", event.getAction(), event.getEntityType(), event.getEntityId());
        auditRepository.saveActivity(event);
    }

    @Async("auditTaskExecutor")
    @EventListener
    public void handleError(ErrorEvent event) {
        log.debug("Audit error: {} at {}", event.getErrorType(), event.getEndpoint());
        auditRepository.saveError(event);
    }

    @Async("auditTaskExecutor")
    @EventListener
    public void handleLogin(LoginEvent event) {
        AuditContext ctx = event.getContext();
        log.debug("Audit login: {} for {}", event.getEventType(), ctx != null ? ctx.getUsername() : "unknown");
        auditRepository.saveLogin(event);
    }
}
