package uz.hemis.service.favorite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.UserFavorite;
import uz.hemis.domain.repository.UserFavoriteRepository;

import java.util.List;
import java.util.UUID;

/**
 * UserFavorite Service - Business Logic for User Favorites
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Manage user's favorite (pinned) menu items</li>
 *   <li>Enforce business rules (max 10, no duplicates)</li>
 *   <li>Support reordering via drag-and-drop</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong></p>
 * <ul>
 *   <li>Maximum 10 favorites per user</li>
 *   <li>No duplicate menu codes per user</li>
 *   <li>Order is user-customizable (order_number field)</li>
 *   <li>New favorites are appended at the end</li>
 * </ul>
 *
 * <p><strong>Transaction Strategy:</strong></p>
 * <ul>
 *   <li>READ operations → {@code @Transactional(readOnly = true)} → Replica DB</li>
 *   <li>WRITE operations → {@code @Transactional} → Master DB</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserFavoriteService {

    /**
     * Maximum number of favorites allowed per user
     */
    private static final int MAX_FAVORITES = 10;

    private final UserFavoriteRepository userFavoriteRepository;

    // =====================================================
    // Read Operations (Replica DB)
    // =====================================================

    /**
     * Get all favorites for a user, ordered by order_number
     *
     * <p>Returns the user's favorite menu items for sidebar/toolbar rendering</p>
     *
     * @param userId the authenticated user's UUID
     * @return list of favorites ordered by order_number ascending
     */
    @Transactional(readOnly = true)
    public List<UserFavorite> getUserFavorites(UUID userId) {
        log.debug("Getting favorites for userId: {}", userId);
        List<UserFavorite> favorites = userFavoriteRepository.findByUserIdOrderByOrderNumberAsc(userId);
        log.debug("Found {} favorites for userId: {}", favorites.size(), userId);
        return favorites;
    }

    // =====================================================
    // Write Operations (Master DB)
    // =====================================================

    /**
     * Add a menu item to user's favorites
     *
     * <p><strong>Business Rules:</strong></p>
     * <ul>
     *   <li>Throws IllegalArgumentException if menu already in favorites</li>
     *   <li>Throws IllegalStateException if maximum limit (10) reached</li>
     *   <li>New favorite is appended at the end (order = current count)</li>
     * </ul>
     *
     * @param userId   the authenticated user's UUID
     * @param menuCode the menu code to add to favorites
     * @return the created UserFavorite entity
     * @throws IllegalArgumentException if menu already in favorites
     * @throws IllegalStateException    if maximum favorites limit reached
     */
    @Transactional
    public UserFavorite addFavorite(UUID userId, String menuCode) {
        log.info("Adding favorite for userId: {}, menuCode: {}", userId, menuCode);

        // Check for duplicate
        if (userFavoriteRepository.existsByUserIdAndMenuCode(userId, menuCode)) {
            log.warn("Duplicate favorite attempt - userId: {}, menuCode: {}", userId, menuCode);
            throw new IllegalArgumentException("Menu item already in favorites");
        }

        // Check maximum limit
        long currentCount = userFavoriteRepository.countByUserId(userId);
        if (currentCount >= MAX_FAVORITES) {
            log.warn("Maximum favorites limit reached - userId: {}, count: {}", userId, currentCount);
            throw new IllegalStateException("Maximum favorites limit reached (" + MAX_FAVORITES + ")");
        }

        // Create and save new favorite
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setMenuCode(menuCode);
        favorite.setOrderNumber((int) currentCount);

        UserFavorite saved = userFavoriteRepository.save(favorite);
        log.info("Favorite added successfully - userId: {}, menuCode: {}, order: {}", userId, menuCode, saved.getOrderNumber());
        return saved;
    }

    /**
     * Remove a menu item from user's favorites
     *
     * <p>Physical delete (not soft delete)</p>
     * <p>No error if favorite does not exist (idempotent)</p>
     *
     * @param userId   the authenticated user's UUID
     * @param menuCode the menu code to remove from favorites
     */
    @Transactional
    public void removeFavorite(UUID userId, String menuCode) {
        log.info("Removing favorite for userId: {}, menuCode: {}", userId, menuCode);
        userFavoriteRepository.deleteByUserIdAndMenuCode(userId, menuCode);
        log.info("Favorite removed successfully - userId: {}, menuCode: {}", userId, menuCode);
    }

    /**
     * Reorder user's favorites
     *
     * <p>Updates the order_number for each favorite based on the provided items</p>
     * <p>Used when user drag-and-drops favorites to rearrange them</p>
     *
     * @param userId the authenticated user's UUID
     * @param items  list of menu codes with their new order numbers
     */
    @Transactional
    public void reorderFavorites(UUID userId, List<FavoriteOrderItem> items) {
        log.info("Reordering {} favorites for userId: {}", items.size(), userId);

        for (FavoriteOrderItem item : items) {
            userFavoriteRepository.findByUserIdAndMenuCode(userId, item.code())
                .ifPresent(fav -> {
                    fav.setOrderNumber(item.order());
                    userFavoriteRepository.save(fav);
                    log.debug("Reordered favorite - menuCode: {}, newOrder: {}", item.code(), item.order());
                });
        }

        log.info("Favorites reordered successfully for userId: {}", userId);
    }

    // =====================================================
    // DTOs
    // =====================================================

    /**
     * DTO for reorder operation
     *
     * @param code  the menu code
     * @param order the new order number
     */
    public record FavoriteOrderItem(String code, int order) {}
}
