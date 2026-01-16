package uz.hemis.common.port.security;

import uz.hemis.common.dto.security.LegacyLoadedUser;

import java.util.Optional;

/**
 * Legacy User Loading Port - Abstraction for loading CUBA users
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
 *   <li>Load legacy user by login for authentication</li>
 *   <li>Support old-hemis (CUBA) compatibility</li>
 *   <li>Abstract away JPA/Hibernate from security module</li>
 * </ul>
 *
 * <p><strong>CUBA Compatibility:</strong></p>
 * <ul>
 *   <li>Queries sec_user table</li>
 *   <li>Handles soft delete (delete_ts check)</li>
 *   <li>Maps CUBA groups to Spring Security roles</li>
 * </ul>
 *
 * @since 2.0.0
 */
public interface LegacyUserLoadingPort {

    /**
     * Find active legacy user by login
     *
     * <p>Searches sec_user table where active = true</p>
     *
     * @param login username (case-insensitive)
     * @return Optional containing LegacyLoadedUser with pre-computed authorities
     */
    Optional<LegacyLoadedUser> findActiveByLogin(String login);

    /**
     * Find legacy user by login (regardless of active status)
     *
     * <p>Used for userId lookup in token generation</p>
     *
     * @param login username (case-insensitive)
     * @return Optional containing LegacyLoadedUser
     */
    Optional<LegacyLoadedUser> findByLogin(String login);
}
