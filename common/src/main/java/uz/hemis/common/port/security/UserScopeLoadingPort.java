package uz.hemis.common.port.security;

import uz.hemis.common.auth.UserScopeData;

import java.util.Optional;
import java.util.UUID;

/**
 * User Scope Loading Port — abstraction for loading a human user's OTM scope inputs.
 *
 * <p><strong>Clean Architecture (mirrors {@link PermissionLoadingPort}):</strong></p>
 * <ul>
 *   <li>Interface in {@code common} (inner contract).</li>
 *   <li>Implementation ({@code UserScopeLoadingAdapter}) in {@code domain} — reads {@code users}.</li>
 *   <li>Consumed by the scope resolver in {@code security}, which never touches JPA directly.</li>
 * </ul>
 *
 * <p>Returns only the raw {@code (user_type, university_code)} of a {@code users} row; the tier→scope
 * policy (which tier is global vs OTM-restricted) lives in the resolver, not here.</p>
 *
 * @since 2.2.0
 */
public interface UserScopeLoadingPort {

    /**
     * Load the scope inputs for a human user.
     *
     * @param userId {@code users.id}
     * @return the user's {@code (user_type, university_code)}, or empty if no such (live) user
     */
    Optional<UserScopeData> loadScope(UUID userId);
}
