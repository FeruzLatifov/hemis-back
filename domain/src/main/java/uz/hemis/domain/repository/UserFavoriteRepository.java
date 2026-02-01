package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.UserFavorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserFavorite Repository - Spring Data JPA
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for user favorite menu items</li>
 *   <li>User-scoped queries (always filtered by userId)</li>
 *   <li>Ordered results by order_number</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All queries are scoped to a specific userId</li>
 *   <li>userId is extracted from JWT in the service layer</li>
 *   <li>No cross-user data access possible</li>
 * </ul>
 *
 * <p><strong>Performance:</strong></p>
 * <ul>
 *   <li>Indexed on user_id for fast lookups</li>
 *   <li>Composite index on (user_id, order_number) for ordered queries</li>
 *   <li>Unique constraint on (user_id, menu_code) prevents duplicates</li>
 * </ul>
 *
 * @see UserFavorite
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UUID> {

    // =====================================================
    // User-Scoped Queries
    // =====================================================

    /**
     * Find all favorites for a user, ordered by order_number
     *
     * <p>Used to display the user's favorites sidebar/toolbar</p>
     * <p>Results are ordered ascending by order_number</p>
     *
     * @param userId the user's UUID
     * @return list of favorites ordered by order_number
     */
    List<UserFavorite> findByUserIdOrderByOrderNumberAsc(UUID userId);

    /**
     * Find a specific favorite by user and menu code
     *
     * <p>Used for reorder operations and duplicate checking</p>
     *
     * @param userId   the user's UUID
     * @param menuCode the menu code
     * @return the favorite if found
     */
    Optional<UserFavorite> findByUserIdAndMenuCode(UUID userId, String menuCode);

    /**
     * Check if a favorite already exists for user and menu code
     *
     * <p>Used to prevent duplicate favorites</p>
     *
     * @param userId   the user's UUID
     * @param menuCode the menu code
     * @return true if favorite exists
     */
    boolean existsByUserIdAndMenuCode(UUID userId, String menuCode);

    // =====================================================
    // Modification Queries
    // =====================================================

    /**
     * Delete a specific favorite by user and menu code
     *
     * <p>Physical delete (not soft delete)</p>
     * <p>Used when user removes a favorite</p>
     *
     * @param userId   the user's UUID
     * @param menuCode the menu code to remove
     */
    @Modifying
    @Transactional
    void deleteByUserIdAndMenuCode(UUID userId, String menuCode);

    // =====================================================
    // Count Queries
    // =====================================================

    /**
     * Count total favorites for a user
     *
     * <p>Used to enforce maximum favorites limit (10)</p>
     *
     * @param userId the user's UUID
     * @return count of favorites
     */
    long countByUserId(UUID userId);
}
