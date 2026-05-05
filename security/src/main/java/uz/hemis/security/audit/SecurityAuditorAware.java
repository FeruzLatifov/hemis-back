package uz.hemis.security.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Spring Data JPA Auditor — populates {@code @CreatedBy}/{@code @LastModifiedBy} columns.
 *
 * <p><strong>OWASP A09 fix:</strong> Avval {@code Optional.empty()} qaytarilardi anonymous
 * user uchun → audit row {@code created_by = NULL} bilan yoziladi → compliance auditor
 * (Vazirlik 7 yil retention) qaysi event'ni kim trigger qilganini ko'ra olmaydi.</p>
 *
 * <p>Endi anonymous holatda "SYSTEM" qaytariladi — audit log har doim non-null.</p>
 */
@Component
public class SecurityAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM_AUDITOR = "SYSTEM";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Compliance fail-safe — never null. SYSTEM marker for anonymous/system events
            // (login attempts, token refresh, scheduled jobs, OAuth client_credentials).
            return Optional.of(SYSTEM_AUDITOR);
        }
        return Optional.of(auth.getName());
    }
}
