package uz.hemis.app.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Audit Request Filter — MDC kontekst o'rnatadi (requestId, clientIp).
 *
 * <p>HTTP so'rovlar nginx tomonidan loglanadi, shuning uchun bu filter
 * faqat correlation ID (requestId) va clientIp ni MDC ga qo'shadi.
 * ErrorEvent faqat GlobalExceptionHandler da publish qilinadi.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuditRequestFilter extends OncePerRequestFilter {

    @Value("${hemis.audit.enabled:false}")
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
            // ErrorEvent faqat GlobalExceptionHandler da publish qilinadi (duplicate oldini olish)
            MDC.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PATTERNS.stream().anyMatch(path::startsWith);
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

}
