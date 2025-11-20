package uz.hemis.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * User Permissions Changed Event
 *
 * <p>Published when a user's permissions or roles are modified</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Evict user permission cache (Redis)</li>
 *   <li>Evict user menu cache (Caffeine + Redis)</li>
 *   <li>Audit trail logging</li>
 *   <li>Notify user of permission changes</li>
 *   <li>Trigger security review</li>
 * </ul>
 *
 * <p><strong>Event Flow:</strong></p>
 * <pre>
 * UserAdminService.updateUserRoles()
 *   → Save user roles
 *   → Publish UserPermissionsChangedEvent
 *   → Event listeners react:
 *      - CacheEvictionEventListener.evictUserCaches()
 *      - AuditService.logPermissionChange()
 *      - NotificationService.notifyUser()
 * </pre>
 *
 * <p><strong>Triggers:</strong></p>
 * <ul>
 *   <li>User role added/removed</li>
 *   <li>Role permissions modified (affects all users with that role)</li>
 *   <li>Direct permission grant/revoke</li>
 *   <li>User account enabled/disabled</li>
 * </ul>
 *
 * <p><strong>Integration Points:</strong></p>
 * <ul>
 *   <li>UserAdminController.updateUserRoles() → publish event</li>
 *   <li>RoleAdminController.updateRolePermissions() → publish event for all affected users</li>
 *   <li>UserAdminController.toggleUserStatus() → publish event</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsChangedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * User ID whose permissions changed
     */
    private UUID userId;

    /**
     * Username (for logging)
     */
    private String username;

    /**
     * Change type: ROLE_ADDED, ROLE_REMOVED, ROLE_MODIFIED, PERMISSION_GRANTED, PERMISSION_REVOKED, USER_DISABLED, USER_ENABLED
     */
    private ChangeType changeType;

    /**
     * Previous roles (before change)
     */
    private List<String> previousRoles;

    /**
     * New roles (after change)
     */
    private List<String> newRoles;

    /**
     * Affected role code (if changeType is ROLE_MODIFIED)
     */
    private String affectedRoleCode;

    /**
     * Timestamp when event was created
     */
    private Instant timestamp;

    /**
     * Admin user who made the change
     */
    private String changedBy;

    /**
     * Reason for change (optional)
     */
    private String reason;

    /**
     * Additional metadata
     */
    private String metadata;

    /**
     * Permission Change Type
     */
    public enum ChangeType {
        /** User role added */
        ROLE_ADDED,

        /** User role removed */
        ROLE_REMOVED,

        /** Role permissions modified (affects all users with this role) */
        ROLE_MODIFIED,

        /** Direct permission granted to user */
        PERMISSION_GRANTED,

        /** Direct permission revoked from user */
        PERMISSION_REVOKED,

        /** User account disabled */
        USER_DISABLED,

        /** User account enabled */
        USER_ENABLED
    }
}
