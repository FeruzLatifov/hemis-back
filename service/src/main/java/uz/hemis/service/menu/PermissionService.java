package uz.hemis.service.menu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Permission;
import uz.hemis.domain.entity.Role;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Permission Service
 * Manages user permissions and access control
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final UserRepository userRepository;

    /**
     * Check if user (by username) can access specific path
     * (Convenience method for controllers)
     */
    public boolean canAccessPath(String username, String path) {
        log.debug("Checking path access for username: {}, path: {}", username, path);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return canAccessPath(user.getId(), path);
    }

    /**
     * Get all permissions for a user (from all roles)
     *
     * <p><strong>NOTE:</strong> This method loads user from database.
     * For better performance, caller should eagerly fetch user with permissions.</p>
     */
    public List<String> getUserPermissions(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", userId);
            return Collections.emptyList();
        }

        User user = userOpt.get();

        // ⚠️ WARNING: This may trigger lazy loading if user not fetched with permissions
        // MenuService should use findByUsernameWithPermissions() to avoid N+1 queries
        Set<Permission> allPermissions = user.getAllPermissions();

        List<String> permissionCodes = allPermissions.stream()
            .map(Permission::getCode)
            .sorted()
            .collect(Collectors.toList());

        log.debug("Loaded {} permissions for user {}", permissionCodes.size(), userId);
        return permissionCodes;
    }

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(UUID userId, String permissionCode) {
        List<String> permissions = getUserPermissions(userId);
        return hasPermissionInternal(permissionCode, permissions);
    }

    /**
     * Check if user can access specific path
     */
    public boolean canAccessPath(UUID userId, String path) {
        // Normalize path: remove leading/trailing slashes
        String normalizedPath = path.replaceAll("^/+|/+$", "");

        // Convert path to permission
        // Example: "/students" -> "students.view"
        String[] parts = normalizedPath.split("/");
        String resource = parts.length > 0 ? parts[0] : "";
        String action = parts.length > 1 ? parts[1] : "view";

        String permission = resource + "." + action;

        return hasPermission(userId, permission);
    }

    /**
     * Check if user has permission (with wildcard support)
     */
    private boolean hasPermissionInternal(String required, List<String> userPermissions) {
        if (required == null || required.isEmpty()) {
            return true; // No permission required
        }

        // Super admin wildcard
        if (userPermissions.contains("*")) {
            return true;
        }

        // Exact match
        if (userPermissions.contains(required)) {
            return true;
        }

        // Wildcard pattern matching
        for (String permission : userPermissions) {
            if (permission.endsWith(".*")) {
                String prefix = permission.substring(0, permission.length() - 2);
                if (required.startsWith(prefix + ".")) {
                    return true;
                }
            }
        }

        return false;
    }
}
