package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.security.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
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
           "LEFT JOIN FETCH u.roles r " +
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
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithPermissions(@Param("id") UUID id);

    /**
     * Find user by ID with university eagerly fetched
     *
     * <p>Used by /app/rest/user/info endpoint to get university name</p>
     * <p>Fetches user → university in single query</p>
     *
     * @param id user ID (UUID)
     * @return user with university if found
     */
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.university " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithUniversity(@Param("id") UUID id);

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

    /**
     * Find user by email (for password reset)
     *
     * @param email email address
     * @return user if found
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.enabled = true")
    Optional<User> findByEmail(@Param("email") String email);

    // =====================================================
    // University Queries
    // =====================================================

    /**
     * Find user by username and university code
     *
     * <p>Used when checking entity-specific user access</p>
     *
     * @param username login username
     * @param universityCode university code
     * @return user if found
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.university.code = :universityCode")
    Optional<User> findByUsernameAndUniversity(
            @Param("username") String username,
            @Param("universityCode") String universityCode
    );

    /**
     * Check if user exists at specific university
     *
     * @param username login username
     * @param universityCode university code
     * @return true if user exists at this university
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u WHERE u.username = :username AND u.university.code = :universityCode")
    boolean existsByUsernameAndUniversity(
            @Param("username") String username,
            @Param("universityCode") String universityCode
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
     * Count users by university code
     *
     * @param universityCode university code
     * @return count of users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.university.code = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

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
    // Admin Filtered Queries
    // =====================================================

    /**
     * Find all users with filters (paginated)
     *
     * <p>Service layer must convert null strings to empty strings before calling this method
     * to avoid Hibernate JPQL null parameter type inference issues
     * (PostgreSQL receives null String as bytea, causing "function lower(bytea) does not exist").</p>
     * <p>Role filter uses subquery to avoid collection JOIN in main query,
     * which would cause in-memory pagination (HHH90003004).</p>
     * <p>University is eagerly loaded via @EntityGraph (ManyToOne — no pagination issue).
     * Roles are lazy loaded within @Transactional.</p>
     *
     * @param search search term (username or fullName, case-insensitive). Empty string = no filter.
     * @param role role code filter. Empty string = no filter.
     * @param university entity code filter. Empty string = no filter.
     * @param enabled enabled status filter. Null = no filter.
     * @param pageable pagination parameters
     * @return page of users matching filters
     */
    @EntityGraph(attributePaths = {"university"})
    @Query("SELECT u FROM User u " +
           "WHERE (:search = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "       OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:role = '' OR u IN (SELECT u2 FROM User u2 JOIN u2.roles r2 WHERE r2.code = :role)) " +
           "AND (:university = '' OR u.university.code = :university) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> findAllFiltered(@Param("search") String search,
                               @Param("role") String role,
                               @Param("university") String university,
                               @Param("enabled") Boolean enabled,
                               Pageable pageable);

    /**
     * Find user by ID with roles and university eagerly fetched
     *
     * <p>Used by UserAdminService for user detail/edit</p>
     *
     * @param id user ID (UUID)
     * @return user with roles and university
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "LEFT JOIN FETCH u.university " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndUniversity(@Param("id") UUID id);

    // =====================================================
    // Cache Warmup Queries
    // =====================================================

    /**
     * Find sample usernames by role code (for cache warmup)
     *
     * <p>Used by MenuCacheWarmup to get sample users per role for pre-caching menus</p>
     * <p>Returns only active users (not deleted, enabled)</p>
     *
     * @param roleCode Role code (e.g., SUPER_ADMIN, OTM_API)
     * @param limit Maximum number of usernames to return (typically 1-2 per role)
     * @return List of usernames with this role
     */
    @Query(value = "SELECT u.username FROM users u " +
                   "JOIN user_role ur ON u.id = ur.user_id " +
                   "JOIN role r ON ur.role_id = r.id " +
                   "WHERE r.code = :roleCode " +
                   "AND u.deleted_at IS NULL " +
                   "AND u.enabled = true " +
                   "ORDER BY u.created_at DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<String> findSampleUsernamesByRoleCode(@Param("roleCode") String roleCode, @Param("limit") int limit);

    // =====================================================
    // Account Lockout (Atomic Updates)
    // =====================================================

    /**
     * Atomically increment failed_attempts and lock account if threshold reached.
     *
     * <p>Single UPDATE avoids read-modify-write race conditions and
     * OptimisticLockException conflicts with @Version.</p>
     *
     * @param username login username
     * @param maxAttempts threshold to lock account
     * @return number of rows updated (0 if user not found)
     */
    @Modifying
    @Query(value = "UPDATE users SET failed_attempts = COALESCE(failed_attempts, 0) + 1, " +
                   "account_non_locked = CASE WHEN COALESCE(failed_attempts, 0) + 1 >= :maxAttempts THEN false ELSE account_non_locked END, " +
                   "locked_at = CASE WHEN COALESCE(failed_attempts, 0) + 1 >= :maxAttempts THEN NOW() ELSE locked_at END " +
                   "WHERE username = :username AND deleted_at IS NULL",
           nativeQuery = true)
    int incrementFailedAttemptsAndLockIfNeeded(@Param("username") String username,
                                               @Param("maxAttempts") int maxAttempts);

    /**
     * Atomically reset failed_attempts on successful login.
     *
     * @param username login username
     * @return number of rows updated
     */
    @Modifying
    @Query(value = "UPDATE users SET failed_attempts = 0, locked_at = NULL, account_non_locked = true " +
                   "WHERE username = :username AND deleted_at IS NULL AND failed_attempts > 0",
           nativeQuery = true)
    int resetFailedAttemptsByUsername(@Param("username") String username);

    /**
     * Auto-unlock accounts where lock has expired (15 minutes).
     *
     * @param minutesAgo threshold: accounts locked before this are unlocked
     * @return number of rows updated
     */
    @Modifying
    @Query(value = "UPDATE users SET account_non_locked = true, failed_attempts = 0, locked_at = NULL " +
                   "WHERE account_non_locked = false AND locked_at IS NOT NULL " +
                   "AND locked_at < NOW() - CAST(:minutesAgo || ' minutes' AS INTERVAL) " +
                   "AND deleted_at IS NULL",
           nativeQuery = true)
    int autoUnlockExpiredAccounts(@Param("minutesAgo") int minutesAgo);

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
