package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.SecUser;

import java.util.Optional;
import java.util.UUID;

/**
 * SecUser Repository - Authentication Queries
 *
 * <p><strong>CRITICAL - Read-Only Repository:</strong></p>
 * <ul>
 *   <li>Only SELECT queries allowed</li>
 *   <li>NO INSERT/UPDATE/DELETE operations</li>
 *   <li>All user management done by old-hemis</li>
 * </ul>
 *
 * <p><strong>Performance:</strong></p>
 * <ul>
 *   <li>Indexed queries (login_lc has UNIQUE INDEX)</li>
 *   <li>Soft delete filter (delete_ts IS NULL)</li>
 *   <li>Active user filter (active = true)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
public interface SecUserRepository extends JpaRepository<SecUser, UUID> {

    /**
     * Find user by login (username) - case-insensitive
     *
     * <p><strong>Usage:</strong> UserDetailsService.loadUserByUsername()</p>
     * <p><strong>Index:</strong> idx_sec_user_uniq_login</p>
     *
     * <p><strong>SQL:</strong></p>
     * <pre>
     * SELECT * FROM sec_user
     * WHERE login_lc = LOWER(:login)
     *   AND delete_ts IS NULL
     *   AND active = true
     * LIMIT 1;
     * </pre>
     *
     * @param login username (case-insensitive)
     * @return Optional<SecUser> if found and active
     */
    @Query("SELECT u FROM SecUser u WHERE LOWER(u.login) = LOWER(:login) AND u.deleteTs IS NULL AND u.active = true")
    Optional<SecUser> findByLoginAndActiveTrue(@Param("login") String login);

    /**
     * Find user by login (any status) - for user lookup
     *
     * <p><strong>Usage:</strong> Check if user exists (regardless of status)</p>
     *
     * @param login username
     * @return Optional<SecUser> if found (including inactive/deleted)
     */
    @Query("SELECT u FROM SecUser u WHERE LOWER(u.login) = LOWER(:login)")
    Optional<SecUser> findByLogin(@Param("login") String login);

    /**
     * Find user by email - for password reset
     *
     * @param email email address
     * @return Optional<SecUser> if found and active
     */
    @Query("SELECT u FROM SecUser u WHERE u.email = :email AND u.deleteTs IS NULL AND u.active = true")
    Optional<SecUser> findByEmailAndActiveTrue(@Param("email") String email);

    /**
     * Find user by university code - for filtering
     *
     * @param universityCode university code
     * @return Optional<SecUser> if found and active
     */
    @Query("SELECT u FROM SecUser u WHERE u.universityCode = :universityCode AND u.deleteTs IS NULL AND u.active = true")
    Optional<SecUser> findByUniversityCodeAndActiveTrue(@Param("universityCode") String universityCode);

    /**
     * Check if user exists by login
     *
     * @param login username
     * @return true if user exists (including inactive)
     */
    @Query("SELECT COUNT(u) > 0 FROM SecUser u WHERE LOWER(u.login) = LOWER(:login)")
    boolean existsByLogin(@Param("login") String login);

    /**
     * Check if active user exists by login
     *
     * @param login username
     * @return true if active user exists
     */
    @Query("SELECT COUNT(u) > 0 FROM SecUser u WHERE LOWER(u.login) = LOWER(:login) AND u.deleteTs IS NULL AND u.active = true")
    boolean existsByLoginAndActiveTrue(@Param("login") String login);

    // =====================================================
    // NOTE: NO WRITE OPERATIONS
    // =====================================================
    // The following methods are INTENTIONALLY NOT IMPLEMENTED:
    // - save()
    // - saveAll()
    // - delete()
    // - deleteById()
    // - deleteAll()
    //
    // Reason: sec_user table is managed by old-hemis (CUBA Platform)
    // hemis-back is READ-ONLY for authentication purposes
    //
    // If you need to create/update users, use old-hemis admin UI:
    // http://localhost:8080/app/#main/sec$User.browse
    // =====================================================
}
