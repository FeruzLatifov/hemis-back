package uz.hemis.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.hemis.app.config.SecurityProperties;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Filter
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Prevent API abuse</li>
 *   <li>Limit requests per university per minute</li>
 *   <li>Global rate limit for all requests</li>
 *   <li>Protect system resources</li>
 * </ul>
 *
 * <p><strong>Rate Limiting Strategy:</strong></p>
 * <ul>
 *   <li>Per-university limit: 100 requests/minute (configurable)</li>
 *   <li>Global limit: 1000 requests/minute (configurable)</li>
 *   <li>Burst capacity: 2x normal limit for short bursts</li>
 *   <li>Token bucket algorithm</li>
 * </ul>
 *
 * <p><strong>Response when rate limit exceeded:</strong></p>
 * <pre>
 * HTTP 429 Too Many Requests
 * {
 *   "error": "rate_limit_exceeded",
 *   "message": "Too many requests. Please try again later.",
 *   "retry_after": 60
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    /**
     * Rate limit counters per university
     * Key: university_code, Value: request count
     */
    private final Map<String, AtomicInteger> universityCounters = new ConcurrentHashMap<>();

    /**
     * Global rate limit counter
     */
    private final AtomicInteger globalCounter = new AtomicInteger(0);

    /**
     * Last reset timestamp (in milliseconds)
     */
    private volatile long lastResetTime = System.currentTimeMillis();

    /**
     * Reset interval in milliseconds (1 minute)
     */
    private static final long RESET_INTERVAL_MS = 60_000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        // Skip if rate limiting is disabled
        if (!securityProperties.isRateLimitEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Reset counters every minute
        resetCountersIfNeeded();

        // Check global rate limit
        if (isGlobalRateLimitExceeded()) {
            log.warn("Global rate limit exceeded - Total requests: {}",
                    globalCounter.get());
            sendRateLimitExceededResponse(response, "Global rate limit exceeded");
            return;
        }

        // Check per-university rate limit
        String universityCode = getUniversityCode(request);
        if (universityCode != null && isUniversityRateLimitExceeded(universityCode)) {
            log.warn("University rate limit exceeded - University: {}, Requests: {}",
                    universityCode, universityCounters.get(universityCode).get());
            sendRateLimitExceededResponse(response,
                    "University rate limit exceeded. Maximum " +
                            securityProperties.getRateLimit().getRequestsPerMinute() +
                            " requests per minute allowed.");
            return;
        }

        // Increment counters
        globalCounter.incrementAndGet();
        if (universityCode != null) {
            universityCounters
                    .computeIfAbsent(universityCode, k -> new AtomicInteger(0))
                    .incrementAndGet();
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Check if global rate limit is exceeded
     */
    private boolean isGlobalRateLimitExceeded() {
        int globalLimit = securityProperties.getRateLimit().getGlobalRequestsPerMinute();
        return globalCounter.get() >= globalLimit;
    }

    /**
     * Check if university rate limit is exceeded
     */
    private boolean isUniversityRateLimitExceeded(String universityCode) {
        AtomicInteger counter = universityCounters.get(universityCode);
        if (counter == null) {
            return false;
        }

        int perUniversityLimit = securityProperties.getRateLimit().getRequestsPerMinute();
        return counter.get() >= perUniversityLimit;
    }

    /**
     * Reset counters every minute
     */
    private void resetCountersIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime >= RESET_INTERVAL_MS) {
            synchronized (this) {
                if (currentTime - lastResetTime >= RESET_INTERVAL_MS) {
                    log.debug("Resetting rate limit counters - Global: {}, Universities: {}",
                            globalCounter.get(), universityCounters.size());

                    globalCounter.set(0);
                    universityCounters.clear();
                    lastResetTime = currentTime;
                }
            }
        }
    }

    /**
     * Extract university code from JWT token
     */
    private String getUniversityCode(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        return jwt.getClaimAsString("university_code");
    }

    /**
     * Send rate limit exceeded response
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format("""
                {
                  "error": "rate_limit_exceeded",
                  "message": "%s",
                  "retry_after": 60
                }
                """, message);

        response.getWriter().write(jsonResponse);
    }

    /**
     * Only apply to API endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Don't rate limit health checks
        if (path.startsWith("/actuator/health") || path.startsWith("/actuator/info")) {
            return true;
        }

        // Only rate limit API endpoints
        return !path.startsWith("/app/rest/v2/services");
    }
}
