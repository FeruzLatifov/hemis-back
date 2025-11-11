package uz.hemis.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.admin.entity.UniversityUser;

import java.util.Optional;

/**
 * Repository for UniversityUser mapping entity
 *
 * Manages user-to-university associations (multi-tenant mapping)
 */
@Repository
public interface UniversityUserRepository extends JpaRepository<UniversityUser, String> {

    /**
     * Find university mapping by user ID
     *
     * @param userId User ID
     * @return Optional UniversityUser mapping
     */
    @Query("SELECT uu FROM UniversityUser uu JOIN FETCH uu.university WHERE uu.user.id = :userId")
    Optional<UniversityUser> findByUserId(@Param("userId") String userId);

    /**
     * Find university mapping by username
     *
     * @param username User login name
     * @return Optional UniversityUser mapping
     */
    @Query("""
        SELECT uu FROM UniversityUser uu
        JOIN FETCH uu.university
        WHERE uu.user.username = :username
        AND uu.user.deletedAt IS NULL
        """)
    Optional<UniversityUser> findByUserUsername(@Param("username") String username);

    /**
     * Check if user has university assigned
     *
     * @param userId User ID
     * @return true if mapping exists
     */
    boolean existsByUserId(String userId);

    /**
     * Find university mapping with full user details
     *
     * @param userId User ID
     * @return Optional UniversityUser with all associations
     */
    @Query("""
        SELECT uu FROM UniversityUser uu
        JOIN FETCH uu.user u
        JOIN FETCH uu.university uni
        WHERE u.id = :userId
        AND u.deletedAt IS NULL
        """)
    Optional<UniversityUser> findByUserIdWithDetails(@Param("userId") String userId);
}
