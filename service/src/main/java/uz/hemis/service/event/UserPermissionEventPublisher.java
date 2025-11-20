package uz.hemis.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uz.hemis.domain.event.UserPermissionsChangedEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * User Permission Event Publisher
 *
 * <p><strong>Purpose:</strong></p>
 * <p>Utility class to publish user permission change events,
 * which trigger automatic cache eviction.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * &#64;Service
 * &#64;RequiredArgsConstructor
 * public class UserAdminService {
 *     private final UserPermissionEventPublisher eventPublisher;
 *     private final UserRepository userRepository;
 *
 *     &#64;Transactional
 *     public void updateUserRoles(UUID userId, List&lt;String&gt; newRoleCodes) {
 *         User user = userRepository.findById(userId)
 *             .orElseThrow(() -> new NotFoundException("User not found"));
 *
 *         List&lt;String&gt; oldRoles = user.getRoleSet().stream()
 *             .map(Role::getCode)
 *             .collect(Collectors.toList());
 *
 *         // Update roles
 *         user.setRoles(newRoleCodes);
 *         userRepository.save(user);
 *
 *         // Publish event → triggers cache eviction
 *         eventPublisher.publishUserRoleChanged(
 *             userId,
 *             user.getUsername(),
 *             oldRoles,
 *             newRoleCodes,
 *             getCurrentUsername()
 *         );
 *     }
 *
 *     &#64;Transactional
 *     public void disableUser(UUID userId) {
 *         User user = userRepository.findById(userId)
 *             .orElseThrow(() -> new NotFoundException("User not found"));
 *
 *         user.setEnabled(false);
 *         userRepository.save(user);
 *
 *         // Publish event → triggers cache eviction
 *         eventPublisher.publishUserDisabled(userId, user.getUsername(), getCurrentUsername());
 *     }
 * }
 * </pre>
 *
 * <p><strong>Event Flow:</strong></p>
 * <pre>
 * UserAdminService.updateUserRoles()
 *   → userPermissionEventPublisher.publishUserRoleChanged()
 *      → applicationEventPublisher.publishEvent(UserPermissionsChangedEvent)
 *         → CacheEvictionEventListener.handleUserPermissionsChanged()
 *            → evict permission cache (Redis)
 *            → evict menu cache (Caffeine + Redis, 4 locales)
 * </pre>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserPermissionEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publish event when user role is changed
     *
     * @param userId User ID
     * @param username Username (for logging)
     * @param previousRoles Previous roles before change
     * @param newRoles New roles after change
     * @param changedBy Admin username who made the change
     */
    public void publishUserRoleChanged(
        UUID userId,
        String username,
        List<String> previousRoles,
        List<String> newRoles,
        String changedBy
    ) {
        log.info("📢 Publishing UserPermissionsChangedEvent: userId={}, type=ROLE_MODIFIED",
            userId);

        UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
            .userId(userId)
            .username(username)
            .changeType(UserPermissionsChangedEvent.ChangeType.ROLE_MODIFIED)
            .previousRoles(previousRoles)
            .newRoles(newRoles)
            .timestamp(Instant.now())
            .changedBy(changedBy)
            .build();

        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event when user role is added
     *
     * @param userId User ID
     * @param username Username
     * @param addedRoleCode Role code that was added
     * @param changedBy Admin username
     */
    public void publishUserRoleAdded(
        UUID userId,
        String username,
        String addedRoleCode,
        String changedBy
    ) {
        log.info("📢 Publishing UserPermissionsChangedEvent: userId={}, type=ROLE_ADDED, role={}",
            userId, addedRoleCode);

        UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
            .userId(userId)
            .username(username)
            .changeType(UserPermissionsChangedEvent.ChangeType.ROLE_ADDED)
            .newRoles(List.of(addedRoleCode))
            .timestamp(Instant.now())
            .changedBy(changedBy)
            .build();

        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event when user role is removed
     *
     * @param userId User ID
     * @param username Username
     * @param removedRoleCode Role code that was removed
     * @param changedBy Admin username
     */
    public void publishUserRoleRemoved(
        UUID userId,
        String username,
        String removedRoleCode,
        String changedBy
    ) {
        log.info("📢 Publishing UserPermissionsChangedEvent: userId={}, type=ROLE_REMOVED, role={}",
            userId, removedRoleCode);

        UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
            .userId(userId)
            .username(username)
            .changeType(UserPermissionsChangedEvent.ChangeType.ROLE_REMOVED)
            .previousRoles(List.of(removedRoleCode))
            .timestamp(Instant.now())
            .changedBy(changedBy)
            .build();

        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event when user account is disabled
     *
     * @param userId User ID
     * @param username Username
     * @param changedBy Admin username
     */
    public void publishUserDisabled(UUID userId, String username, String changedBy) {
        log.info("📢 Publishing UserPermissionsChangedEvent: userId={}, type=USER_DISABLED",
            userId);

        UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
            .userId(userId)
            .username(username)
            .changeType(UserPermissionsChangedEvent.ChangeType.USER_DISABLED)
            .timestamp(Instant.now())
            .changedBy(changedBy)
            .reason("User account disabled by administrator")
            .build();

        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event when user account is enabled
     *
     * @param userId User ID
     * @param username Username
     * @param changedBy Admin username
     */
    public void publishUserEnabled(UUID userId, String username, String changedBy) {
        log.info("📢 Publishing UserPermissionsChangedEvent: userId={}, type=USER_ENABLED",
            userId);

        UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
            .userId(userId)
            .username(username)
            .changeType(UserPermissionsChangedEvent.ChangeType.USER_ENABLED)
            .timestamp(Instant.now())
            .changedBy(changedBy)
            .reason("User account enabled by administrator")
            .build();

        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event when role permissions are modified (affects all users with that role)
     *
     * @param roleCode Role code (e.g., "TEACHER", "STUDENT")
     * @param changedBy Admin username
     */
    public void publishRolePermissionsModified(String roleCode, String changedBy) {
        log.warn("📢 Publishing UserPermissionsChangedEvent: type=ROLE_MODIFIED, roleCode={}",
            roleCode);
        log.warn("   ⚠️  This will evict caches for ALL users with role: {}", roleCode);

        UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
            .userId(null) // No specific user (affects all users with this role)
            .username(null)
            .changeType(UserPermissionsChangedEvent.ChangeType.ROLE_MODIFIED)
            .affectedRoleCode(roleCode)
            .timestamp(Instant.now())
            .changedBy(changedBy)
            .reason("Role permissions modified - affects all users with role: " + roleCode)
            .build();

        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event when permission is granted directly to user (not via role)
     *
     * @param userId User ID
     * @param username Username
     * @param permissionCode Permission code
     * @param changedBy Admin username
     */
    public void publishPermissionGranted(
        UUID userId,
        String username,
        String permissionCode,
        String changedBy
    ) {
        log.info("📢 Publishing UserPermissionsChangedEvent: userId={}, type=PERMISSION_GRANTED, permission={}",
            userId, permissionCode);

        UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
            .userId(userId)
            .username(username)
            .changeType(UserPermissionsChangedEvent.ChangeType.PERMISSION_GRANTED)
            .timestamp(Instant.now())
            .changedBy(changedBy)
            .metadata("permission:" + permissionCode)
            .build();

        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event when permission is revoked directly from user
     *
     * @param userId User ID
     * @param username Username
     * @param permissionCode Permission code
     * @param changedBy Admin username
     */
    public void publishPermissionRevoked(
        UUID userId,
        String username,
        String permissionCode,
        String changedBy
    ) {
        log.info("📢 Publishing UserPermissionsChangedEvent: userId={}, type=PERMISSION_REVOKED, permission={}",
            userId, permissionCode);

        UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
            .userId(userId)
            .username(username)
            .changeType(UserPermissionsChangedEvent.ChangeType.PERMISSION_REVOKED)
            .timestamp(Instant.now())
            .changedBy(changedBy)
            .metadata("permission:" + permissionCode)
            .build();

        applicationEventPublisher.publishEvent(event);
    }
}
