package uz.hemis.common.port.security;

import uz.hemis.common.dto.security.LoadedUser;

import java.util.Optional;
import java.util.UUID;

/**
 * User Loading Port - Abstraction for loading modern users
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
 *   <li>Load user by username for authentication</li>
 *   <li>Load user by ID with permissions for authorization</li>
 *   <li>Abstract away JPA/Hibernate from security module</li>
 * </ul>
 *
 * @since 2.0.0
 */
public interface UserLoadingPort {

    /**
     * Find user by username for authentication
     *
     * @param username login username
     * @return Optional containing LoadedUser with pre-computed authorities
     */
    Optional<LoadedUser> findByUsername(String username);

    /**
     * Find user by ID with permissions for authorization
     *
     * <p>This method eagerly loads roles and permissions for caching</p>
     *
     * @param userId user ID (UUID)
     * @return Optional containing LoadedUser with permissions
     */
    Optional<LoadedUser> findByIdWithPermissions(UUID userId);

    /**
     * Check if user exists by username
     *
     * @param username login username
     * @return true if user exists
     */
    boolean existsByUsername(String username);
}
