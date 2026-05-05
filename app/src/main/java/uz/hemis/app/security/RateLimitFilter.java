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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Filter
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Prevent API abuse</li>
 *   <li>Limit requests per university per minute (authenticated)</li>
 *   <li>Limit requests per IP per minute (anonymous + login brute-force)</li>
 *   <li>Stricter limit for login/token endpoints</li>
 *   <li>Global rate limit for all requests</li>
 * </ul>
 *
 * <p><strong>Rate Limiting Strategy:</strong></p>
 * <ul>
 *   <li>Per-university: 100 requests/minute (configurable)</li>
 *   <li>Per-IP: 100 requests/minute (anonymous fallback)</li>
 *   <li>Per-IP login: 10 requests/minute (brute-force protection)</li>
 *   <li>Global: 1000 requests/minute (configurable)</li>
 *   <li>Fixed-window algorithm (1-minute reset)</li>
 * </ul>
 *
 * <p><strong>⚠️ Production limitation:</strong> in-memory ConcurrentHashMap counters
 * faqat bir Pod ichida ishlaydi. Distributed deploy (224 OTM × N Pod) uchun
 * Bucket4j + Redis backed implementation tavsiya etiladi (alohida sprint).</p>
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

    /** Per-university counters (authenticated requests). */
    private final Map<String, AtomicInteger> universityCounters = new ConcurrentHashMap<>();

    /** Per-IP counters (anonymous + general fallback). */
    private final Map<String, AtomicInteger> ipCounters = new ConcurrentHashMap<>();

    /** Per-IP login counters (brute-force protection — stricter limit). */
    private final Map<String, AtomicInteger> loginIpCounters = new ConcurrentHashMap<>();

    /** Global request counter. */
    private final AtomicInteger globalCounter = new AtomicInteger(0);

    /** Last reset timestamp (ms). */
    private volatile long lastResetTime = System.currentTimeMillis();

    private static final long RESET_INTERVAL_MS = 60_000;

    /** Endpoint'lar brute-force himoyasi kerak (loginRequestsPerMinutePerIp limit qo'llaniladi). */
    private static final Set<String> LOGIN_ENDPOINTS = Set.of(
            "/api/v1/web/auth/login",
            "/app/rest/v2/oauth/token",
            "/api/v1/external/oauth/token",
            "/api/v1/university/oauth/token"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        if (!securityProperties.isRateLimitEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        resetCountersIfNeeded();

        // 1. Global limit (eng tashqi)
        if (isGlobalRateLimitExceeded()) {
            log.warn("Global rate limit exceeded — total: {}", globalCounter.get());
            sendRateLimitExceededResponse(response, "Global rate limit exceeded");
            return;
        }

        // 2. Login endpoint — stricter per-IP limit (brute-force protection)
        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();
        if (isLoginEndpoint(requestPath)) {
            if (isLoginIpRateLimitExceeded(clientIp)) {
                log.warn("Login rate limit exceeded — IP: {}, path: {}, count: {}",
                        clientIp, requestPath, loginIpCounters.get(clientIp).get());
                sendRateLimitExceededResponse(response,
                        "Login attempt limit exceeded. Maximum " +
                                securityProperties.getRateLimit().getLoginRequestsPerMinutePerIp() +
                                " login attempts per minute allowed per IP.");
                return;
            }
            loginIpCounters.computeIfAbsent(clientIp, k -> new AtomicInteger(0)).incrementAndGet();
        }

        // 3. Per-university limit (authenticated bilan JWT)
        String universityCode = getUniversityCode();
        if (universityCode != null && isUniversityRateLimitExceeded(universityCode)) {
            log.warn("University rate limit exceeded — university: {}, count: {}",
                    universityCode, universityCounters.get(universityCode).get());
            sendRateLimitExceededResponse(response,
                    "University rate limit exceeded. Maximum " +
                            securityProperties.getRateLimit().getRequestsPerMinute() +
                            " requests per minute allowed.");
            return;
        }

        // 4. Per-IP limit fallback (anonymous yoki university'siz request)
        if (universityCode == null && isIpRateLimitExceeded(clientIp)) {
            log.warn("IP rate limit exceeded — IP: {}, count: {}",
                    clientIp, ipCounters.get(clientIp).get());
            sendRateLimitExceededResponse(response,
                    "IP rate limit exceeded. Maximum " +
                            securityProperties.getRateLimit().getRequestsPerMinutePerIp() +
                            " requests per minute allowed per IP.");
            return;
        }

        // 5. Counter'larni o'sib boring
        globalCounter.incrementAndGet();
        if (universityCode != null) {
            universityCounters.computeIfAbsent(universityCode, k -> new AtomicInteger(0)).incrementAndGet();
        } else {
            ipCounters.computeIfAbsent(clientIp, k -> new AtomicInteger(0)).incrementAndGet();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isGlobalRateLimitExceeded() {
        return globalCounter.get() >= securityProperties.getRateLimit().getGlobalRequestsPerMinute();
    }

    private boolean isUniversityRateLimitExceeded(String universityCode) {
        AtomicInteger counter = universityCounters.get(universityCode);
        if (counter == null) return false;
        return counter.get() >= securityProperties.getRateLimit().getRequestsPerMinute();
    }

    private boolean isIpRateLimitExceeded(String ip) {
        AtomicInteger counter = ipCounters.get(ip);
        if (counter == null) return false;
        return counter.get() >= securityProperties.getRateLimit().getRequestsPerMinutePerIp();
    }

    private boolean isLoginIpRateLimitExceeded(String ip) {
        AtomicInteger counter = loginIpCounters.get(ip);
        if (counter == null) return false;
        return counter.get() >= securityProperties.getRateLimit().getLoginRequestsPerMinutePerIp();
    }

    private boolean isLoginEndpoint(String path) {
        return LOGIN_ENDPOINTS.contains(path);
    }

    /** Fixed-window reset: counters har 1 daqiqada nolga. */
    private void resetCountersIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime >= RESET_INTERVAL_MS) {
            synchronized (this) {
                if (currentTime - lastResetTime >= RESET_INTERVAL_MS) {
                    log.debug("Resetting rate limit counters — global: {}, universities: {}, IPs: {}, login IPs: {}",
                            globalCounter.get(),
                            universityCounters.size(),
                            ipCounters.size(),
                            loginIpCounters.size());
                    globalCounter.set(0);
                    universityCounters.clear();
                    ipCounters.clear();
                    loginIpCounters.clear();
                    lastResetTime = currentTime;
                }
            }
        }
    }

    /** JWT'dan university_code ni olish. Anonymous request'da null. */
    private String getUniversityCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt.getClaimAsString("university_code");
    }

    /** Real client IP — proxy header'lar faqat trusted proxy'dan keldi. */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            // Trusted-proxy validation soddalashtirilgan: production deploy K8s ingress orqali
            // X-Forwarded-For ni faqat ingress qo'shadi. Local'da remoteAddr 127.0.0.1.
            return xff.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitExceededResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", "60");

        String jsonResponse = String.format("""
                {
                  "error": "rate_limit_exceeded",
                  "message": "%s",
                  "retry_after": 60
                }
                """, message);
        response.getWriter().write(jsonResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.startsWith("/actuator/health") || path.startsWith("/actuator/info")) {
            return true;
        }
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources") || path.startsWith("/webjars/")) {
            return true;
        }
        if (path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png")
                || path.endsWith(".ico") || path.endsWith(".svg")) {
            return true;
        }

        return !path.startsWith("/app/rest/") && !path.startsWith("/api/");
    }
}
