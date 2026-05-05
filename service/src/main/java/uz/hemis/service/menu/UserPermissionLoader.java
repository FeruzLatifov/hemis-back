package uz.hemis.service.menu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.security.Permission;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * User Permission Loader — alohida bean (Spring AOP self-invocation trap'ini yopish uchun).
 *
 * <p><strong>Sabab:</strong> {@link PermissionService} ichida {@code getUserPermissions()}
 * `@Cacheable` edi, lekin same-class chaqiriqlar (hasPermission, canAccessPath) AOP proxy'dan
 * o'tmaydi — cache silently bypass qilinardi. Har auth check'da DB hit (har request).</p>
 *
 * <p><strong>Pattern:</strong> {@code StudentLoader} (service/CLAUDE.md "AOP self-invocation").
 * Loader alohida bean — Spring proxy chaqirilishini kafolatlaydi.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserPermissionLoader {

    private final UserRepository userRepository;

    /**
     * Foydalanuvchi permissionlari (cached, immutable list).
     *
     * <p>Cache key: {@code userPermissions:#userId}. TTL — DashboardCacheConfig (30m default).</p>
     */
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<String> load(UUID userId) {
        Optional<User> userOpt = userRepository.findByIdWithPermissions(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", userId);
            return Collections.emptyList();
        }

        User user = userOpt.get();
        Set<Permission> allPermissions = user.getAllPermissions();

        List<String> permissionCodes = allPermissions.stream()
                .map(Permission::getCode)
                .sorted()
                .toList();

        log.debug("Loaded {} permissions for user {} (cache miss)", permissionCodes.size(), userId);
        return permissionCodes;
    }
}
