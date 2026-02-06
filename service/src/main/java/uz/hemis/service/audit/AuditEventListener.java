package uz.hemis.service.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
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
public class AuditEventListener {

    private final AuditRepository auditRepository;

    @Async
    @EventListener
    public void handleActivity(ActivityEvent event) {
        log.debug("Audit activity: {} {} {}", event.getAction(), event.getEntityType(), event.getEntityId());
        auditRepository.saveActivity(event);
    }

    @Async
    @EventListener
    public void handleError(ErrorEvent event) {
        log.debug("Audit error: {} at {}", event.getErrorType(), event.getEndpoint());
        auditRepository.saveError(event);
    }

    @Async
    @EventListener
    public void handleLogin(LoginEvent event) {
        AuditContext ctx = event.getContext();
        log.debug("Audit login: {} for {}", event.getEventType(), ctx != null ? ctx.getUsername() : "unknown");
        auditRepository.saveLogin(event);
    }
}
