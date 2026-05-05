package uz.hemis.app.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId Filter — har request uchun unique correlation ID generate qiladi va MDC'ga qo'yadi.
 *
 * <p><strong>Behavior:</strong></p>
 * <ul>
 *   <li>Kelgan request'da {@code X-Request-ID} (yoki {@code X-Trace-Id}) header bor bo'lsa,
 *       shu qiymat MDC.{@code traceId} ga qo'yiladi (distributed tracing — gateway/ingress
 *       traceId'ni propagate qiladi).</li>
 *   <li>Yo'q bo'lsa — yangi UUID (8 chars short) generate qilinadi.</li>
 *   <li>Response header {@code X-Request-ID} ga ham yoziladi (klientga qaytariladi —
 *       support uchun foydali: foydalanuvchi error xabar bilan birga ID ham bo'ladi).</li>
 * </ul>
 *
 * <p><strong>Filter chain order (app/CLAUDE.md):</strong></p>
 * <ol>
 *   <li><strong>TraceIdFilter</strong> — eng tashqi (HIGHEST_PRECEDENCE). MDC har log uchun.</li>
 *   <li>RequestLoggingFilter / AuditRequestFilter — body capture, audit log</li>
 *   <li>RateLimitFilter — limit before auth (login brute-force protection)</li>
 *   <li>Spring Security — JWT validation, @PreAuthorize</li>
 *   <li>Controller dispatch</li>
 * </ol>
 *
 * <p><strong>Sentry/observability integration:</strong> {@code traceId} MDC entry log
 * pattern'da {@code [%X{traceId:-}]} bilan har log line'ga qo'shiladi (logback-spring.xml).</p>
 *
 * @since 2.1.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String MDC_TRACE_ID = "traceId";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String traceId = extractOrGenerate(request);

        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader(HEADER_REQUEST_ID, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }

    /** Header'dan oladi (gateway propagate) yoki yangi 8-char UUID generate qiladi. */
    private String extractOrGenerate(HttpServletRequest request) {
        String fromHeader = request.getHeader(HEADER_REQUEST_ID);
        if (fromHeader == null || fromHeader.isBlank()) {
            fromHeader = request.getHeader(HEADER_TRACE_ID);
        }
        if (fromHeader != null && !fromHeader.isBlank()) {
            // Defensive: max 64 chars (log injection bo'lmasligi uchun)
            return fromHeader.length() > 64 ? fromHeader.substring(0, 64) : fromHeader;
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
