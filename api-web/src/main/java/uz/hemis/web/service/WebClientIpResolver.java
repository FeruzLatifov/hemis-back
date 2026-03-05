package uz.hemis.web.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Resolves client IP from HttpServletRequest with trusted-proxy validation.
 *
 * <p>Only trusts X-Forwarded-For / X-Real-IP when the direct connection
 * comes from a known proxy (localhost, RFC 1918 private nets, or explicitly
 * configured addresses).</p>
 *
 * @since 2.0.0
 */
@Component
@Slf4j
public class WebClientIpResolver {

    @Value("${app.security.trusted-proxies:}")
    private String trustedProxiesConfig;

    /**
     * Extract the real client IP, respecting proxy headers only from trusted sources.
     */
    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String xff = request.getHeader("X-Forwarded-For");
        String xRealIP = request.getHeader("X-Real-IP");

        log.debug("IP resolve: remoteAddr={}, X-Forwarded-For={}, X-Real-IP={}", remoteAddr, xff, xRealIP);

        if (isTrustedProxy(remoteAddr)) {
            if (xff != null && !xff.isEmpty()) {
                String clientIp = xff.split(",")[0].trim();
                log.debug("Trusted proxy ({}), using X-Forwarded-For: {}", remoteAddr, clientIp);
                return clientIp;
            }

            if (xRealIP != null && !xRealIP.isEmpty()) {
                log.debug("Trusted proxy ({}), using X-Real-IP: {}", remoteAddr, xRealIP);
                return xRealIP;
            }
        } else if (xff != null) {
            log.warn("Untrusted client ({}) sent X-Forwarded-For header - IGNORED", remoteAddr);
        }

        return remoteAddr;
    }

    private boolean isTrustedProxy(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        // Configured trusted proxies
        if (trustedProxiesConfig != null && !trustedProxiesConfig.isEmpty()) {
            List<String> trusted = Arrays.stream(trustedProxiesConfig.split(","))
                    .map(String::trim)
                    .filter(ip -> !ip.isEmpty())
                    .toList();
            if (trusted.contains(ipAddress)) {
                return true;
            }
        }

        // Localhost
        if (ipAddress.equals("127.0.0.1") ||
            ipAddress.equals("::1") ||
            ipAddress.equals("0:0:0:0:0:0:0:1")) {
            return true;
        }

        // RFC 1918 private ranges
        String[] parts = ipAddress.split("\\.");
        if (parts.length == 4) {
            try {
                int first = Integer.parseInt(parts[0]);
                int second = Integer.parseInt(parts[1]);
                if (first == 10) return true;                                   // 10.0.0.0/8
                if (first == 172 && second >= 16 && second <= 31) return true;  // 172.16.0.0/12
                if (first == 192 && second == 168) return true;                 // 192.168.0.0/16
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }
}
