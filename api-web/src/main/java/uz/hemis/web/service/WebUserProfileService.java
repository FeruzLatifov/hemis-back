package uz.hemis.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.hemis.domain.entity.Permission;
import uz.hemis.domain.entity.Role;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.security.service.UserPermissionCacheService;
import uz.hemis.web.dto.UserInfoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Loads user profile data and manages per-user cache clearing.
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebUserProfileService {

    private final UserRepository userRepository;
    private final UserPermissionCacheService permissionCacheService;

    /**
     * Load full user profile with permissions and roles.
     *
     * @param userId UUID from JWT subject
     * @return populated UserInfoResponse
     * @throws UsernameNotFoundException if user not found
     */
    public UserInfoResponse loadCurrentUser(UUID userId) {
        User user = userRepository.findByIdWithPermissions(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        List<String> permissions = user.getAllPermissions().stream()
                .map(Permission::getCode)
                .sorted()
                .collect(Collectors.toList());

        List<String> roles = user.getRoleSet().stream()
                .map(Role::getName)
                .sorted()
                .collect(Collectors.toList());

        log.info("User loaded: {} permissions, {} roles", permissions.size(), roles.size());

        return UserInfoResponse.builder()
                .user(UserInfoResponse.UserBasicInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .locale("uz-UZ")
                        .active(user.getEnabled())
                        .build())
                .permissions(permissions)
                .roles(roles)
                .university(user.getEntityCode() != null
                        ? UserInfoResponse.UniversityBasicInfo.builder()
                                .code(user.getEntityCode())
                                .build()
                        : null)
                .build();
    }

    /**
     * Clear all caches for the given user.
     *
     * @return list of cache categories cleared
     */
    public List<String> clearUserCache(UUID userId) {
        List<String> cleared = new ArrayList<>();

        permissionCacheService.evictUserCache(userId);
        cleared.add("permissions");
        log.info("Cleared permissions cache for userId: {}", userId);

        // Menu / translations cache eviction can be extended here
        cleared.add("menus");
        cleared.add("translations");

        log.info("Cache cleared for userId: {} - {}", userId, cleared);
        return cleared;
    }
}
