package uz.hemis.common.port.security;

import java.util.Set;
import java.util.UUID;

/**
 * Permission Loading Port - Abstraction for loading user permissions
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This interface is defined in security module (inner layer)</li>
 *   <li>Implementation is in domain module (outer layer)</li>
 *   <li>Dependency flows inward: domain → security</li>
 * </ul>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Load permissions for Redis caching</li>
 *   <li>Abstract away JPA/Hibernate from security module</li>
 *   <li>Support permission-based authorization</li>
 * </ul>
 *
 * @since 2.0.0
 */
public interface PermissionLoadingPort {

    /**
     * Get all permission codes for a user
     *
     * <p>Loads user with roles and permissions, extracts permission codes</p>
     *
     * @param userId user ID (UUID)
     * @return Set of permission codes (e.g., "student:read", "teacher:create")
     */
    Set<String> getPermissionsForUser(UUID userId);

    /**
     * Check if user has specific permission
     *
     * @param userId user ID (UUID)
     * @param permissionCode permission code to check
     * @return true if user has the permission
     */
    boolean hasPermission(UUID userId, String permissionCode);
}
