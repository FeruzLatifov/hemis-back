package uz.hemis.domain.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Permission;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.common.port.security.PermissionLoadingPort;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Permission Loading Adapter - Implements PermissionLoadingPort
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This adapter is in domain module (outer layer)</li>
 *   <li>Implements port defined in security module (inner layer)</li>
 *   <li>Provides permission loading without exposing JPA entities</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionLoadingAdapter implements PermissionLoadingPort {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Set<String> getPermissionsForUser(UUID userId) {
        log.debug("Loading permissions for userId: {}", userId);

        return userRepository.findByIdWithPermissions(userId)
                .map(user -> user.getAllPermissions().stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toSet()))
                .orElseGet(() -> {
                    log.warn("User not found for permission loading: {}", userId);
                    return Set.of();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(UUID userId, String permissionCode) {
        Set<String> permissions = getPermissionsForUser(userId);
        return permissions.contains(permissionCode);
    }
}
