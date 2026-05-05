package uz.hemis.security.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extract caller IP from an HTTP request, honouring the {@code X-Forwarded-For} /
 * {@code X-Real-IP} headers set by the Nginx load balancer (see architecture.md).
 *
 * <p>Single source of truth for IP resolution so OAuth controllers, the IP whitelist
 * filter and rate limiters all see the same address for a given request.</p>
 *
 * <p><strong>SECURITY (audit 2026-05-05):</strong> XFF trust unconditional. Production
 * deploy talab qiladi: <em>only Nginx LB</em> direct-hits Spring app — pod-to-pod traffic
 * yopiq. Agar app internet-facing yoki internal proxy-bypass bo'lsa, XFF spoofing orqali
 * IP whitelist va rate-limiter aldab bo'ladi. Production checklist:</p>
 * <ul>
 *   <li>K8s NetworkPolicy: faqat ingress controller'dan {@code 8080/tcp} ruxsat.</li>
 *   <li>Nginx ingress: {@code proxy_set_header X-Forwarded-For $remote_addr} (overwrite,
 *       not append) — eski header'larni client tomonidan olib tashlab tashlaydi.</li>
 * </ul>
 *
 * <p><strong>Future improvement:</strong> {@code @Component} refactor + trusted-proxies
 * CIDR config. {@code request.getRemoteAddr()} trusted CIDR'da bo'lsa XFF ishlat,
 * aks holda {@code getRemoteAddr()} qaytar. 10+ caller refactor — alohida sprint.</p>
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
