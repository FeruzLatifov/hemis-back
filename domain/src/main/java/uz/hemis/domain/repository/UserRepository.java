package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Repository - Spring Data JPA
 *
 * <p><strong>CRITICAL - OAuth2 Authentication:</strong></p>
 * <ul>
 *   <li>Used by OAuth2 Token endpoint for username/password validation</li>
 *   <li>Queries only active users (enabled = true, delete_ts IS NULL)</li>
 *   <li>NO DELETE operations (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All queries filter deleted records (@Where clause)</li>
 *   <li>Password never exposed in queries (use UserDetailsService)</li>
 *   <li>Account lockout considered in active user queries</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, UUID> {

    // =====================================================
    // Authentication Queries
    // =====================================================

    /**
     * Find user by username (for OAuth2 login)
     *
     * <p>Used by UserDetailsService to load user during authentication</p>
     * <p>Returns user even if disabled (disable check in service layer)</p>
     *
     * @param username login username
     * @return user if found (including disabled users)
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by username with roles eagerly fetched
     *
     * <p>Used by UserInfo endpoint to avoid lazy loading issues</p>
     *
     * @param username login username
     * @return user with roles if found
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roleSet WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Find user by username with roles AND permissions eagerly fetched
     *
     * <p>Used by MenuService to avoid N+1 lazy loading issues</p>
     * <p>Fetches user → roles → permissions in single query</p>
     *
     * @param username login username
     * @return user with roles and permissions if found
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roleSet r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.username = :username")
    Optional<User> findByUsernameWithPermissions(@Param("username") String username);

    /**
     * Find user by ID with roles AND permissions eagerly fetched
     *
     * <p>Used by /auth/me endpoint to avoid N+1 lazy loading issues</p>
     * <p>Fetches user → roles → permissions in single query</p>
     *
     * @param id user ID (UUID)
     * @return user with roles and permissions if found
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roleSet r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithPermissions(@Param("id") UUID id);

    /**
     * Find active user by username
     *
     * <p><strong>Active means:</strong></p>
     * <ul>
     *   <li>enabled = true</li>
     *   <li>delete_ts IS NULL (not deleted)</li>
     *   <li>account_non_locked = true</li>
     * </ul>
     *
     * @param username login username
     * @return user if found and active
     */
    @Query("SELECT u FROM User u WHERE u.username = :username " +
           "AND u.enabled = true AND u.accountNonLocked = true")
    Optional<User> findActiveByUsername(@Param("username") String username);

    /**
     * Check if username exists
     *
     * <p>Used for unique username validation</p>
     *
     * @param username login username
     * @return true if username exists (even if disabled)
     */
    boolean existsByUsername(String username);

    // =====================================================
    // University Queries
    // =====================================================

    /**
     * Find user by username and entity code
     *
     * <p>Used when checking entity-specific user access</p>
     *
     * @param username login username
     * @param entityCode entity code (university/organization)
     * @return user if found
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.entityCode = :entityCode")
    Optional<User> findByUsernameAndUniversity(
            @Param("username") String username,
            @Param("entityCode") String entityCode
    );

    /**
     * Check if user exists at specific entity
     *
     * @param username login username
     * @param entityCode entity code (university/organization)
     * @return true if user exists at this entity
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u WHERE u.username = :username AND u.entityCode = :entityCode")
    boolean existsByUsernameAndUniversity(
            @Param("username") String username,
            @Param("entityCode") String entityCode
    );

    // =====================================================
    // Account Management
    // =====================================================

    /**
     * Find users with locked accounts
     *
     * <p>Used for admin dashboard / monitoring</p>
     *
     * @return list of locked users
     */
    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false")
    java.util.List<User> findLockedAccounts();

    /**
     * Count active users
     *
     * @return count of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true AND u.accountNonLocked = true")
    long countActiveUsers();

    /**
     * Count users by entity code
     *
     * @param entityCode entity code (university/organization)
     * @return count of users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.entityCode = :entityCode")
    long countByUniversity(@Param("entityCode") String entityCode);

    /**
     * Get university code by user ID
     *
     * <p>Used by Student Service to get current user's university</p>
     *
     * @param userId user UUID
     * @return university code (e.g., "401") or null if not assigned
     */
    @Query("SELECT u.university.code FROM User u WHERE u.id = :userId")
    Optional<String> findUniversityCodeById(@Param("userId") UUID userId);

    // =====================================================
    // Cache Warmup Queries
    // =====================================================

    /**
     * Find sample usernames by role code (for cache warmup)
     *
     * <p>Used by MenuCacheWarmup to get sample users per role for pre-caching menus</p>
     * <p>Returns only active users (not deleted, enabled)</p>
     *
     * @param roleCode Role code (e.g., SUPER_ADMIN, UNIVERSITY_ADMIN)
     * @param limit Maximum number of usernames to return (typically 1-2 per role)
     * @return List of usernames with this role
     */
    @Query(value = "SELECT u.username FROM users u " +
                   "JOIN user_roles ur ON u.id = ur.user_id " +
                   "JOIN roles r ON ur.role_id = r.id " +
                   "WHERE r.code = :roleCode " +
                   "AND u.deleted_at IS NULL " +
                   "AND u.enabled = true " +
                   "ORDER BY u.created_at DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<String> findSampleUsernamesByRoleCode(@Param("roleCode") String roleCode, @Param("limit") int limit);

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // The following inherited methods are available but PROHIBITED:
    // - void deleteById(UUID id)
    // - void delete(User entity)
    // - void deleteAll()
    //
    // These methods will FAIL at database level:
    // - Database role has NO DELETE permission
    // - Application enforces NDG (Non-Deletion Guarantee)
    //
    // For soft delete:
    // - Use service layer to set deleteTs = NOW()
    // - Queries automatically exclude deleted records (@Where clause)
    // =====================================================
}
