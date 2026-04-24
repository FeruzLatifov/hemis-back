package uz.hemis.security.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import uz.hemis.common.auth.ClientType;
import uz.hemis.common.auth.SubjectInfo;
import uz.hemis.common.auth.SubjectType;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default {@link SubjectResolver} — reads HEMIS custom JWT claims.
 *
 * <p><strong>JWT claim contract:</strong></p>
 * <pre>
 * {
 *   "sub":              "&lt;uuid&gt;",               // subject id (users.id OR oauth_client.id)
 *   "typ":              "USER" | "CLIENT",            // discriminator (new; "USER" assumed if missing)
 *   "username":         "&lt;username | client_id&gt;",
 *   "scope":            "rest-api ...",               // space-delimited legacy scopes
 *   "authorities":      ["students.view", "..."],    // optional explicit authorities
 *   "university_code":  "101",                        // optional tenant scope
 *   "employee_id":      "&lt;uuid&gt;",                // USER only
 *   "client_type":      "UNIVERSITY_BACKEND|..."     // CLIENT only
 * }
 * </pre>
 *
 * <p>Backward-compat: JWTs issued by the legacy {@code TokenService#generateToken}
 * path have no {@code typ} claim — they are treated as {@code USER} tokens so existing
 * 224 OTM integrations keep working unchanged.</p>
 *
 * @since 2.1.0
 */
@Component
@Slf4j
public class DefaultSubjectResolver implements SubjectResolver {

    static final String CLAIM_TYP = "typ";
    static final String CLAIM_USERNAME = "username";
    static final String CLAIM_UNIVERSITY_CODE = "university_code";
    static final String CLAIM_EMPLOYEE_ID = "employee_id";
    static final String CLAIM_CLIENT_TYPE = "client_type";
    static final String CLAIM_AUTHORITIES = "authorities";

    @Override
    public Optional<SubjectInfo> resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(resolveFromJwt(jwtAuth.getToken(), jwtAuth.getAuthorities()));
        }
        return Optional.empty();
    }

    @Override
    public SubjectInfo resolveFromJwt(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT is required");
        }
        java.util.List<String> rawAuthorities = jwt.getClaimAsStringList(CLAIM_AUTHORITIES);
        Set<? extends GrantedAuthority> authorities = rawAuthorities == null
                ? Set.of()
                : rawAuthorities.stream()
                        .map(GrantedAuthorityAdapter::authority)
                        .collect(Collectors.toSet());
        return resolveFromJwt(jwt, authorities);
    }

    private SubjectInfo resolveFromJwt(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT is required");
        }
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("JWT missing 'sub' claim");
        }

        SubjectType type = resolveType(jwt);
        UUID id = parseUuid(subject, "sub");
        String subjectName = Optional.ofNullable(jwt.getClaimAsString(CLAIM_USERNAME))
                .filter(s -> !s.isBlank())
                .orElse(subject);
        Set<String> authStrings = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
        String universityCode = jwt.getClaimAsString(CLAIM_UNIVERSITY_CODE);

        if (type == SubjectType.CLIENT) {
            ClientType clientType = parseClientType(jwt.getClaimAsString(CLAIM_CLIENT_TYPE));
            return SubjectInfo.forClient(id, subjectName, authStrings, universityCode, clientType);
        }

        UUID employeeId = Optional.ofNullable(jwt.getClaimAsString(CLAIM_EMPLOYEE_ID))
                .filter(s -> !s.isBlank())
                .map(s -> parseUuid(s, CLAIM_EMPLOYEE_ID))
                .orElse(null);
        return SubjectInfo.forUser(id, subjectName, authStrings, universityCode, employeeId);
    }

    private SubjectType resolveType(Jwt jwt) {
        String raw = jwt.getClaimAsString(CLAIM_TYP);
        if (raw == null || raw.isBlank()) {
            // Legacy tokens (pre-Phase-2) have no typ claim — treat as USER for backward-compat.
            return SubjectType.USER;
        }
        try {
            return SubjectType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown JWT 'typ' claim value '{}' — defaulting to USER", raw);
            return SubjectType.USER;
        }
    }

    private ClientType parseClientType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ClientType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown JWT 'client_type' claim value '{}'", raw);
            return null;
        }
    }

    private UUID parseUuid(String raw, String claimName) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "JWT claim '" + claimName + "' is not a valid UUID: " + raw, ex);
        }
    }

    /** Tiny adapter to wrap a string as {@link GrantedAuthority} without pulling in Spring's SimpleGrantedAuthority. */
    @FunctionalInterface
    private interface GrantedAuthorityAdapter extends GrantedAuthority {
        static GrantedAuthority authority(String role) {
            return () -> role;
        }
    }
}
