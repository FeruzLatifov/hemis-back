package uz.hemis.security.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extract caller IP from an HTTP request, honouring the {@code X-Forwarded-For} /
 * {@code X-Real-IP} headers set by the Nginx load balancer (see architecture.md).
 *
 * <p>Single source of truth for IP resolution so OAuth controllers, the IP whitelist
 * filter and rate limiters all see the same address for a given request.</p>
 *
 * @since 2.1.0
 */
public final class HttpClientIpResolver {

    private HttpClientIpResolver() {
    }

    /**
     * Returns the originating client IP. Order of precedence:
     * <ol>
     *   <li>First non-blank segment of {@code X-Forwarded-For}</li>
     *   <li>{@code X-Real-IP}</li>
     *   <li>{@link HttpServletRequest#getRemoteAddr()}</li>
     * </ol>
     */
    public static String resolve(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            for (String part : forwarded.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
