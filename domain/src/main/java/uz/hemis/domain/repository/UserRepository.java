package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.User;

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
     * Find user by username and university
     *
     * <p>Used when checking university-specific user access</p>
     *
     * @param username login username
     * @param universityCode university code
     * @return user if found
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.university = :university")
    Optional<User> findByUsernameAndUniversity(
            @Param("username") String username,
            @Param("university") String universityCode
    );

    /**
     * Check if user exists at specific university
     *
     * @param username login username
     * @param universityCode university code
     * @return true if user exists at this university
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u WHERE u.username = :username AND u.university = :university")
    boolean existsByUsernameAndUniversity(
            @Param("username") String username,
            @Param("university") String universityCode
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
     * Count users by university
     *
     * @param universityCode university code
     * @return count of users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.university = :university")
    long countByUniversity(@Param("university") String universityCode);

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
