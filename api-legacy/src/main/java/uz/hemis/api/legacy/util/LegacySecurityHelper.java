package uz.hemis.api.legacy.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.Optional;

/**
 * Shared security helper for legacy API controllers.
 * Extracts university code and username from JWT security context.
 *
 * @since 1.5.4
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LegacySecurityHelper {

    private final UserRepository userRepository;

    /**
     * Extract university code from security context.
     * Priority:
     * 1. User entity's entity_code field
     * 2. Username format extraction (otmXXX -> XXX)
     *
     * @return university code or null
     */
    public String getUniversityCodeFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("No authentication found in security context");
            return null;
        }

        String username = extractUsername(auth);
        if (username == null) {
            return null;
        }

        // 1. From user table entity_code
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                String entityCode = userOpt.get().getEntityCode();
                if (entityCode != null && !entityCode.isEmpty()) {
                    log.debug("University code from user.entity_code: {}", entityCode);
                    return entityCode;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get university code from database for user: {}", username, e);
        }

        // 2. From username format (otm401 -> 401)
        String universityCode = extractUniversityCodeFromUsername(username);
        if (universityCode != null) {
            log.debug("University code extracted from username {}: {}", username, universityCode);
            return universityCode;
        }

        log.warn("Could not determine university code for user: {}", username);
        return null;
    }

    /**
     * Get current authenticated username.
     *
     * @return username or "system"
     */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "system";
        }

        String username = extractUsername(auth);
        return username != null ? username : "system";
    }

    private String extractUsername(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaimAsString("username");
            return username != null ? username : jwt.getSubject();
        }
        return auth.getName();
    }

    private String extractUniversityCodeFromUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        if (username.toLowerCase().startsWith("otm")) {
            String code = username.substring(3);
            if (code.matches("\\d+")) {
                return code;
            }
        }
        return null;
    }
}
