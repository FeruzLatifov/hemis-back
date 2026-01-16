package uz.hemis.common.port.security;

import java.util.Optional;
import java.util.UUID;

/**
 * User Identification Port - Abstraction for user ID lookup
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
 *   <li>Convert username to userId for JWT generation</li>
 *   <li>Support hybrid lookup (users → sec_user fallback)</li>
 *   <li>Abstract away JPA/Hibernate from security module</li>
 * </ul>
 *
 * @since 2.0.0
 */
public interface UserIdentificationPort {

    /**
     * Get user ID by username using hybrid lookup
     *
     * <p><strong>Lookup Strategy:</strong></p>
     * <ol>
     *   <li>First try users table (modern system)</li>
     *   <li>If not found, fallback to sec_user table (legacy CUBA)</li>
     * </ol>
     *
     * @param username login username
     * @return Optional containing user ID (UUID)
     */
    Optional<UUID> getUserIdByUsername(String username);

    /**
     * Check which source a user comes from
     *
     * @param username login username
     * @return "users" for modern system, "sec_user" for legacy, null if not found
     */
    String getUserSource(String username);
}
