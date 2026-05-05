package uz.hemis.service.menu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.repository.UserRepository;

import java.util.List;
import java.util.UUID;

/**
 * Permission Service
 * Manages user permissions and access control.
 *
 * <p><strong>Cache strategy:</strong> {@link UserPermissionLoader} alohida bean — Spring AOP
 * self-invocation trap'i yopiladi (avval {@code @Cacheable} shu class'da edi va same-class
 * {@code hasPermission}/{@code canAccessPath} chaqiriqlari cache'ni silently bypass qilardi).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final UserRepository userRepository;
    private final UserPermissionLoader permissionLoader;

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
     * Get all permissions for a user (delegates to cached loader).
     *
     * <p>Eager fetch + cache + AOP-proxy-aware (loader is a separate bean).</p>
     */
    public List<String> getUserPermissions(UUID userId) {
        return permissionLoader.load(userId);
    }

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(UUID userId, String permissionCode) {
        // ✅ Loader chaqiriladi — Spring proxy orqali, cache hit
        List<String> permissions = permissionLoader.load(userId);
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
