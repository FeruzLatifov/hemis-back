package uz.hemis.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.admin.entity.User;

import java.util.Optional;

/**
 * Repository for User entity (CUBA sec$User)
 *
 * Provides database access methods for user management
 * Supports soft delete pattern (deletedAt != null)
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find active user by username (login)
     *
     * @param username User login name
     * @return Optional user (only non-deleted)
     */
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    /**
     * Find user by username (including soft-deleted)
     *
     * @param username User login name
     * @return Optional user
     */
    Optional<User> findByUsername(String username);

    /**
     * Find active user by email
     *
     * @param email User email address
     * @return Optional user (only non-deleted)
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Check if username exists (active users only)
     *
     * @param username User login name
     * @return true if exists and active
     */
    boolean existsByUsernameAndDeletedAtIsNull(String username);

    /**
     * Check if email exists (active users only)
     *
     * @param email User email
     * @return true if exists and active
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Find user with university association
     * Uses JOIN FETCH to avoid N+1 queries
     *
     * @param username User login name
     * @return Optional user with university loaded
     */
    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.universityUser uu
        LEFT JOIN FETCH uu.university
        WHERE u.username = :username
        AND u.deletedAt IS NULL
        """)
    Optional<User> findByUsernameWithUniversity(@Param("username") String username);

    /**
     * Find user by ID with university
     *
     * @param id User ID
     * @return Optional user with university
     */
    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.universityUser uu
        LEFT JOIN FETCH uu.university
        WHERE u.id = :id
        AND u.deletedAt IS NULL
        """)
    Optional<User> findByIdWithUniversity(@Param("id") String id);

    /**
     * Count active users
     *
     * @return Number of active users
     */
    long countByDeletedAtIsNull();

    /**
     * Count users by university
     *
     * @param universityCode University code
     * @return Number of users in university
     */
    @Query("""
        SELECT COUNT(u) FROM User u
        JOIN u.universityUser uu
        WHERE uu.university.code = :universityCode
        AND u.deletedAt IS NULL
        """)
    long countByUniversityCode(@Param("universityCode") String universityCode);
}
