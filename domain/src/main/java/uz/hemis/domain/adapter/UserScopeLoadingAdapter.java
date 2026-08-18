package uz.hemis.domain.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    // Deliberately NOT @Cacheable. Two reasons:
    //  1. Anti-pattern: this is a PK-indexed projection (~1ms). A Redis L2 hit (~50ms: serialize +
    //     deserialize + network) is SLOWER than the DB read — cf. UniversityService.findByCode, which
    //     dropped its cache for the same reason.
    //  2. Correctness: UserScopeData is a *record* (implicitly final). The cache ObjectMapper uses
    //     activateDefaultTyping(NON_FINAL), which writes no @class for final types, so a cached value
    //     cannot be read back ("missing type id property '@class'") — every cache HIT threw. Serving
    //     it live from the DB is both faster and correct. (The dormant "userScope" cache config/evict
    //     can be re-enabled only with a serializer that emits @class for records.)
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
