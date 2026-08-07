package uz.hemis.domain.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.auth.UserScopeData;
import uz.hemis.common.port.security.UserScopeLoadingPort;
import uz.hemis.domain.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * User Scope Loading Adapter — implements {@link UserScopeLoadingPort}.
 *
 * <p>Domain-layer adapter (mirrors {@link PermissionLoadingAdapter}) that reads a user's
 * {@code (user_type, university_code)} without exposing JPA entities to the {@code security} module.
 * The tier→scope policy is NOT here — this only projects the raw columns.</p>
 *
 * @since 2.2.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserScopeLoadingAdapter implements UserScopeLoadingPort {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    // Spring unwraps the Optional return, so #result is the UserScopeData (null when empty);
    // a bare "== null" is the correct "don't cache misses" guard. Calling .isEmpty() here would
    // target UserScopeData (a record, no isEmpty()) → SpelEvaluationException on every hit.
    @Cacheable(value = "userScope", key = "#userId", unless = "#result == null")
    public Optional<UserScopeData> loadScope(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        Optional<UserScopeData> scope = userRepository.findScopeById(userId);
        if (scope.isEmpty()) {
            log.warn("Scope load: no user found for id {}", userId);
        }
        return scope;
    }
}
