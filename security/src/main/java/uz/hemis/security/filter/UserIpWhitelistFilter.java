package uz.hemis.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.hemis.common.auth.SubjectInfo;
import uz.hemis.common.auth.SubjectType;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.security.auth.SubjectResolver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

/**
 * Enforces {@code users.allowed_ip_cidr} for authenticated human requests.
 *
 * <p>Runs after the JWT authentication filter — for each authenticated USER subject,
 * loads the account's IP whitelist (if any) and denies the request when the caller
 * IP falls outside the allowed CIDRs.</p>
 *
 * <p><strong>Backward-compat:</strong> when {@code allowed_ip_cidr} is {@code NULL}
 * or empty (true for all 339 legacy users), the filter is a no-op. OTM password-flow
 * clients are unaffected until an admin explicitly configures a whitelist.</p>
 *
 * <p>CLIENT subjects are skipped — their whitelist is enforced earlier in
 * {@code OAuthClientAuthenticationService} before the token is ever issued.</p>
 *
 * @since 2.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserIpWhitelistFilter extends OncePerRequestFilter {

    private final SubjectResolver subjectResolver;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<SubjectInfo> subject = subjectResolver.resolve(authentication);

        if (subject.isEmpty() || subject.get().type() != SubjectType.USER) {
            chain.doFilter(request, response);
            return;
        }

        User user = userRepository.findById(subject.get().id()).orElse(null);
        if (user == null) {
            chain.doFilter(request, response);
            return;
        }

        List<String> allowedCidr = user.getAllowedIpCidr();
        if (allowedCidr == null || allowedCidr.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String remoteIp = extractClientIp(request);
        if (!isIpAllowed(allowedCidr, remoteIp)) {
            log.warn("IP whitelist denied: user='{}' ip='{}'", user.getUsername(), remoteIp);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"error\":\"access_denied\",\"error_description\":\"IP address not permitted for this account\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Extract caller IP honouring {@code X-Forwarded-For} / {@code X-Real-IP}
     * (Nginx load balancer in front of the app — see architecture.md).
     */
    static String extractClientIp(HttpServletRequest request) {
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

    /**
     * Check whether {@code remoteIp} matches any CIDR / exact-IP entry.
     * Fails closed: unparseable IPs are denied.
     */
    static boolean isIpAllowed(List<String> allowedCidr, String remoteIp) {
        if (remoteIp == null || remoteIp.isBlank()) {
            return false;
        }
        InetAddress remote;
        try {
            remote = InetAddress.getByName(remoteIp);
        } catch (UnknownHostException ex) {
            return false;
        }
        for (String entry : allowedCidr) {
            if (entry == null || entry.isBlank()) continue;
            if (matches(entry.trim(), remote)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String cidr, InetAddress remote) {
        int slash = cidr.indexOf('/');
        try {
            if (slash < 0) {
                return InetAddress.getByName(cidr).equals(remote);
            }
            InetAddress network = InetAddress.getByName(cidr.substring(0, slash));
            int prefix = Integer.parseInt(cidr.substring(slash + 1));
            byte[] netBytes = network.getAddress();
            byte[] remBytes = remote.getAddress();
            if (netBytes.length != remBytes.length) return false;
            int fullBytes = prefix / 8;
            int remainderBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (netBytes[i] != remBytes[i]) return false;
            }
            if (remainderBits == 0) return true;
            int mask = 0xFF << (8 - remainderBits);
            return (netBytes[fullBytes] & mask) == (remBytes[fullBytes] & mask);
        } catch (Exception ex) {
            return false;
        }
    }
}
