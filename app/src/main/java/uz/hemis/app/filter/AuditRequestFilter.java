package uz.hemis.app.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.hemis.common.audit.AuditContext;
import uz.hemis.common.audit.ErrorEvent;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Audit Request Filter — MDC kontekst o'rnatadi va 5xx xatolarni audit logga yozadi.
 *
 * <p>HTTP so'rovlar nginx tomonidan loglanadi, shuning uchun bu filter
 * faqat correlation ID (requestId) va clientIp ni MDC ga qo'shadi,
 * hamda 5xx server xatolarni ErrorEvent sifatida publish qiladi.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class AuditRequestFilter extends OncePerRequestFilter {

    private final ApplicationEventPublisher eventPublisher;

    @Value("${hemis.audit.enabled:true}")
    private boolean auditEnabled;

    private static final List<String> SKIP_PATTERNS = List.of(
            "/actuator", "/swagger-ui", "/v3/api-docs", "/favicon.ico",
            "/css/", "/js/", "/images/", "/fonts/", "/webjars/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (!auditEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put("requestId", requestId);
        MDC.put("clientIp", getClientIp(request));

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Faqat 5xx server xatolarni loglaymiz
            if (response.getStatus() >= 500) {
                try {
                    AuditContext context = buildContext(request, requestId);
                    eventPublisher.publishEvent(ErrorEvent.builder()
                            .context(context)
                            .errorType("HTTP_" + response.getStatus())
                            .errorMessage("Server error on " + request.getMethod() + " " + request.getRequestURI())
                            .endpoint(request.getRequestURI())
                            .build());
                } catch (Exception e) {
                    log.debug("Failed to publish error event: {}", e.getMessage());
                }
            }

            MDC.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PATTERNS.stream().anyMatch(path::startsWith);
    }

    private AuditContext buildContext(HttpServletRequest request, String requestId) {
        AuditContext.AuditContextBuilder builder = AuditContext.builder()
                .ip(getClientIp(request))
                .userAgent(truncate(request.getHeader("User-Agent"), 512))
                .sessionId(request.getHeader("X-Session-ID"))
                .requestId(requestId)
                .endpoint(request.getRequestURI());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName();
            if (name != null) {
                try {
                    builder.userId(UUID.fromString(name));
                } catch (IllegalArgumentException ignored) {
                    builder.username(name);
                }
            }
        }

        return builder.build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLen) {
        if (value == null) return null;
        return value.length() > maxLen ? value.substring(0, maxLen) : value;
    }
}
