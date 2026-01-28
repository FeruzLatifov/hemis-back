package uz.hemis.domain.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Permission;
import uz.hemis.domain.entity.Role;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.common.dto.security.LoadedUser;
import uz.hemis.common.port.security.UserLoadingPort;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Loading Adapter - Implements UserLoadingPort
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This adapter is in domain module (outer layer)</li>
 *   <li>Implements port defined in security module (inner layer)</li>
 *   <li>Converts JPA entities to security DTOs</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserLoadingAdapter implements UserLoadingPort {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<LoadedUser> findByUsername(String username) {
        log.debug("Loading user by username: {}", username);

        return userRepository.findByUsername(username)
                .map(this::toLoadedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LoadedUser> findByIdWithPermissions(UUID userId) {
        log.debug("Loading user by ID with permissions: {}", userId);

        return userRepository.findByIdWithPermissions(userId)
                .map(this::toLoadedUserWithPermissions);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Convert User entity to LoadedUser DTO
     *
     * <p>Computes authorities from roles and permissions</p>
     */
    private LoadedUser toLoadedUser(User user) {
        Set<String> authorities = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        // Extract authorities from roles
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // Add role as authority (e.g., "ROLE_SUPER_ADMIN")
                authorities.add("ROLE_" + role.getCode());

                // Add all permissions from this role
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(permission.getCode());
                        permissions.add(permission.getCode());
                    }
                }
            }
        }

        return LoadedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .enabled(user.getEnabled() != null && user.getEnabled())
                .accountNonLocked(user.getAccountNonLocked() != null && user.getAccountNonLocked())
                .deleted(user.isDeleted())
                .authorities(authorities)
                .permissions(permissions)
                .build();
    }

    /**
     * Convert User entity to LoadedUser DTO with full permissions
     *
     * <p>Used for permission caching - includes all nested permissions</p>
     */
    private LoadedUser toLoadedUserWithPermissions(User user) {
        Set<String> permissions = user.getAllPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        Set<String> authorities = new HashSet<>(permissions);

        // Add role codes as authorities
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                authorities.add("ROLE_" + role.getCode());
            }
        }

        return LoadedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .enabled(user.getEnabled() != null && user.getEnabled())
                .accountNonLocked(user.getAccountNonLocked() != null && user.getAccountNonLocked())
                .deleted(user.isDeleted())
                .authorities(authorities)
                .permissions(permissions)
                .build();
    }
}
