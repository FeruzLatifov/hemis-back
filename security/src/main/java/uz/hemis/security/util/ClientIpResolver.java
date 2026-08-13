package uz.hemis.security.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Trusted-proxy-aware client-IP resolver for security decisions (rate-limiting, the
 * OAuth-client IP allowlist, and the forensic audit-trail IP) — where a spoofable
 * value is a real bypass or a poisoned evidence record.
 *
 * <p><strong>Rule:</strong> {@code X-Forwarded-For}/{@code X-Real-IP} are honoured ONLY
 * when the direct socket peer ({@link HttpServletRequest#getRemoteAddr()}) is a trusted
 * proxy. The real client is found by walking {@code X-Forwarded-For} <strong>right-to-left,
 * skipping trusted-proxy hops</strong> — the first untrusted address is the client. This is
 * robust whether the ingress <em>overwrites</em> or <em>appends</em> the header (a leftmost
 * read is only safe under overwrite). An untrusted (public) peer's forwarded headers are
 * ignored and the socket peer is used, closing the spoof.</p>
 *
 * <p>Trust set: the configured {@code app.security.trusted-proxies} list plus loopback. Only
 * when NO explicit list is configured (dev) are RFC-1918 private ranges trusted as a
 * convenience (the K8s ingress→pod hop). In prod, set the ingress IP(s)/CIDR explicitly so a
 * compromised in-cluster peer cannot forge headers.</p>
 *
 * <p>Mirrors {@code WebClientIpResolver} (api-web) — that one cannot be reused here because
 * the security module must not depend on api-web.</p>
 */
@Component
@Slf4j
public class ClientIpResolver {

    @Value("${app.security.trusted-proxies:}")
    private String trustedProxiesConfig;

    /** Real client IP; forwarded headers trusted only from a trusted proxy, else the socket peer. */
    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String xff = request.getHeader("X-Forwarded-For");
        if (isTrustedProxy(remoteAddr)) {
            if (xff != null && !xff.isBlank()) {
                String[] hops = xff.split(",");
                // Right-to-left: skip trusted-proxy hops; first untrusted = real client.
                for (int i = hops.length - 1; i >= 0; i--) {
                    String hop = hops[i].trim();
                    if (hop.isEmpty() || isTrustedProxy(hop)) {
                        continue;
                    }
                    return hop;
                }
                // Every hop was a trusted proxy → the leftmost is closest to the client.
                String first = hops[0].trim();
                if (!first.isEmpty()) {
                    return first;
                }
            }
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isBlank()) {
                return xRealIp.trim();
            }
        } else if (xff != null) {
            log.warn("Untrusted client {} sent X-Forwarded-For — IGNORED (using socket peer for rate-limit/allowlist/audit)",
                    remoteAddr);
        }
        return remoteAddr;
    }

    private boolean isTrustedProxy(String ip) {
        String norm = normalize(ip);
        if (norm == null) {
            return false;
        }
        boolean hasExplicitConfig = trustedProxiesConfig != null && !trustedProxiesConfig.isBlank();
        if (hasExplicitConfig) {
            for (String t : trustedProxiesConfig.split(",")) {
                String candidate = t.trim();
                if (!candidate.isEmpty() && (norm.equals(candidate) || ip.equals(candidate))) {
                    return true;
                }
            }
            // Explicit list configured (prod): trust ONLY it + loopback — no blanket private ranges.
            return isLoopback(norm);
        }
        // No explicit list (dev): trust loopback + RFC-1918 (the ingress→pod hop).
        return isLoopback(norm) || isPrivate(norm);
    }

    /** Strip an IPv4-mapped IPv6 prefix (e.g. {@code ::ffff:10.0.0.1} → {@code 10.0.0.1}). */
    private static String normalize(String ip) {
        if (ip == null || ip.isBlank()) {
            return null;
        }
        String s = ip.trim();
        return s.startsWith("::ffff:") ? s.substring("::ffff:".length()) : s;
    }

    private static boolean isLoopback(String ip) {
        return ip.equals("127.0.0.1") || ip.startsWith("127.")
                || ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1");
    }

    private static boolean isPrivate(String ip) {
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.matches("^172\\.(1[6-9]|2\\d|3[01])\\..*");
    }
}
