package uz.hemis.security.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.hemis.common.auth.SubjectInfo;

import java.util.Optional;

/**
 * Resolve a {@link SubjectInfo} from a Spring Security {@link Authentication}
 * or a decoded {@link Jwt}.
 *
 * <p>Acts as the single adapter between Spring Security's principal and HEMIS's
 * polymorphic subject model (USER vs CLIENT). Controllers and services should
 * always go through this resolver rather than reading JWT claims directly.</p>
 *
 * @see SubjectInfo
 * @since 2.1.0
 */
public interface SubjectResolver {

    /**
     * Resolve the current subject from Spring Security {@code Authentication}.
     *
     * @param authentication security principal from the current request
     * @return subject info, or {@code Optional.empty()} if unauthenticated / anonymous
     */
    Optional<SubjectInfo> resolve(Authentication authentication);

    /**
     * Resolve a subject directly from a decoded JWT.
     *
     * <p>The {@code typ} claim switches behaviour:</p>
     * <ul>
     *   <li>{@code "USER"} or missing → {@link uz.hemis.common.auth.SubjectType#USER}</li>
     *   <li>{@code "CLIENT"} → {@link uz.hemis.common.auth.SubjectType#CLIENT}</li>
     * </ul>
     *
     * @param jwt decoded JWT (signature already validated)
     * @return subject info
     * @throws IllegalArgumentException if required claims are missing
     */
    SubjectInfo resolveFromJwt(Jwt jwt);
}
