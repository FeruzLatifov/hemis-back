package uz.hemis.service.favorite;

import uz.hemis.common.exception.BadRequestException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.security.UserFavorite;
import uz.hemis.domain.repository.UserFavoriteRepository;
import uz.hemis.service.favorite.dto.FavoriteReorderItem;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserFavoriteService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getUserFavorites - returns ordered list, empty list</li>
 *   <li>addFavorite - happy path, duplicate check, max limit</li>
 *   <li>removeFavorite - existing and non-existing (idempotent)</li>
 *   <li>reorderFavorites - updates order numbers, skips missing</li>
 * </ul>
 *
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserFavoriteService Unit Tests")
class UserFavoriteServiceTest {

    @Mock
    private UserFavoriteRepository userFavoriteRepository;

    @InjectMocks
    private UserFavoriteService userFavoriteService;

    @Captor
    private ArgumentCaptor<UserFavorite> favoriteCaptor;

    private UUID userId;
    private UserFavorite dashboardFavorite;
    private UserFavorite registryFavorite;
    private UserFavorite ratingFavorite;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        dashboardFavorite = new UserFavorite();
        dashboardFavorite.setId(UUID.randomUUID());
        dashboardFavorite.setUserId(userId);
        dashboardFavorite.setMenuCode("dashboard");
        dashboardFavorite.setOrderNumber(0);
        dashboardFavorite.setCreatedAt(LocalDateTime.now().minusDays(3));

        registryFavorite = new UserFavorite();
        registryFavorite.setId(UUID.randomUUID());
        registryFavorite.setUserId(userId);
        registryFavorite.setMenuCode("registry-e-reestr");
        registryFavorite.setOrderNumber(1);
        registryFavorite.setCreatedAt(LocalDateTime.now().minusDays(2));

        ratingFavorite = new UserFavorite();
        ratingFavorite.setId(UUID.randomUUID());
        ratingFavorite.setUserId(userId);
        ratingFavorite.setMenuCode("rating-university");
        ratingFavorite.setOrderNumber(2);
        ratingFavorite.setCreatedAt(LocalDateTime.now().minusDays(1));
    }

    // =====================================================
    // getUserFavorites tests
    // =====================================================

    @Nested
    @DisplayName("getUserFavorites")
    class GetUserFavorites {

        @Test
        @DisplayName("returns favorites ordered by order number")
        void returnsFavoritesOrderedByOrderNumber() {
            // Given
            List<UserFavorite> favorites = List.of(dashboardFavorite, registryFavorite, ratingFavorite);
            when(userFavoriteRepository.findByUserIdOrderByOrderNumberAsc(userId)).thenReturn(favorites);

            // When
            List<UserFavorite> result = userFavoriteService.getUserFavorites(userId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getMenuCode()).isEqualTo("dashboard");
            assertThat(result.get(0).getOrderNumber()).isEqualTo(0);
            assertThat(result.get(1).getMenuCode()).isEqualTo("registry-e-reestr");
            assertThat(result.get(1).getOrderNumber()).isEqualTo(1);
            assertThat(result.get(2).getMenuCode()).isEqualTo("rating-university");
            assertThat(result.get(2).getOrderNumber()).isEqualTo(2);

            verify(userFavoriteRepository).findByUserIdOrderByOrderNumberAsc(userId);
        }

        @Test
        @DisplayName("returns empty list when user has no favorites")
        void returnsEmptyListWhenNoFavorites() {
            // Given
            when(userFavoriteRepository.findByUserIdOrderByOrderNumberAsc(userId))
                    .thenReturn(Collections.emptyList());

            // When
            List<UserFavorite> result = userFavoriteService.getUserFavorites(userId);

            // Then
            assertThat(result).isEmpty();

            verify(userFavoriteRepository).findByUserIdOrderByOrderNumberAsc(userId);
        }

        @Test
        @DisplayName("each favorite belongs to the correct user")
        void eachFavoriteBelongsToCorrectUser() {
            // Given
            List<UserFavorite> favorites = List.of(dashboardFavorite, registryFavorite);
            when(userFavoriteRepository.findByUserIdOrderByOrderNumberAsc(userId)).thenReturn(favorites);

            // When
            List<UserFavorite> result = userFavoriteService.getUserFavorites(userId);

            // Then
            assertThat(result).allMatch(fav -> fav.getUserId().equals(userId));
        }
    }

    // =====================================================
    // addFavorite tests
    // =====================================================

    @Nested
    @DisplayName("addFavorite")
    class AddFavorite {

        @Test
        @DisplayName("happy path - creates new favorite with correct order number")
        void happyPath_createsNewFavoriteWithCorrectOrder() {
            // Given
            String menuCode = "student-list";
            when(userFavoriteRepository.existsByUserIdAndMenuCode(userId, menuCode)).thenReturn(false);
            when(userFavoriteRepository.countByUserId(userId)).thenReturn(3L);

            UserFavorite savedFavorite = new UserFavorite();
            savedFavorite.setId(UUID.randomUUID());
            savedFavorite.setUserId(userId);
            savedFavorite.setMenuCode(menuCode);
            savedFavorite.setOrderNumber(3);
            savedFavorite.setCreatedAt(LocalDateTime.now());

            when(userFavoriteRepository.save(any(UserFavorite.class))).thenReturn(savedFavorite);

            // When
            UserFavorite result = userFavoriteService.addFavorite(userId, menuCode);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getMenuCode()).isEqualTo("student-list");
            assertThat(result.getOrderNumber()).isEqualTo(3);

            verify(userFavoriteRepository).existsByUserIdAndMenuCode(userId, menuCode);
            verify(userFavoriteRepository).countByUserId(userId);
            verify(userFavoriteRepository).save(favoriteCaptor.capture());

            UserFavorite captured = favoriteCaptor.getValue();
            assertThat(captured.getUserId()).isEqualTo(userId);
            assertThat(captured.getMenuCode()).isEqualTo(menuCode);
            assertThat(captured.getOrderNumber()).isEqualTo(3);
        }

        @Test
        @DisplayName("first favorite - order number is 0")
        void firstFavorite_orderNumberIsZero() {
            // Given
            String menuCode = "dashboard";
            when(userFavoriteRepository.existsByUserIdAndMenuCode(userId, menuCode)).thenReturn(false);
            when(userFavoriteRepository.countByUserId(userId)).thenReturn(0L);

            UserFavorite savedFavorite = new UserFavorite();
            savedFavorite.setId(UUID.randomUUID());
            savedFavorite.setUserId(userId);
            savedFavorite.setMenuCode(menuCode);
            savedFavorite.setOrderNumber(0);

            when(userFavoriteRepository.save(any(UserFavorite.class))).thenReturn(savedFavorite);

            // When
            UserFavorite result = userFavoriteService.addFavorite(userId, menuCode);

            // Then
            assertThat(result.getOrderNumber()).isEqualTo(0);

            verify(userFavoriteRepository).save(favoriteCaptor.capture());
            assertThat(favoriteCaptor.getValue().getOrderNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("duplicate menu code - throws IllegalArgumentException")
        void duplicateMenuCode_throwsIllegalArgumentException() {
            // Given
            String menuCode = "dashboard";
            when(userFavoriteRepository.existsByUserIdAndMenuCode(userId, menuCode)).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> userFavoriteService.addFavorite(userId, menuCode))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already in favorites");

            verify(userFavoriteRepository).existsByUserIdAndMenuCode(userId, menuCode);
            verify(userFavoriteRepository, never()).countByUserId(any());
            verify(userFavoriteRepository, never()).save(any());
        }

        @Test
        @DisplayName("maximum limit reached (10) - throws IllegalStateException")
        void maximumLimitReached_throwsIllegalStateException() {
            // Given
            String menuCode = "new-menu-item";
            when(userFavoriteRepository.existsByUserIdAndMenuCode(userId, menuCode)).thenReturn(false);
            when(userFavoriteRepository.countByUserId(userId)).thenReturn(10L);

            // When / Then
            assertThatThrownBy(() -> userFavoriteService.addFavorite(userId, menuCode))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Maximum favorites limit reached")
                    .hasMessageContaining("10");

            verify(userFavoriteRepository).existsByUserIdAndMenuCode(userId, menuCode);
            verify(userFavoriteRepository).countByUserId(userId);
            verify(userFavoriteRepository, never()).save(any());
        }

        @Test
        @DisplayName("at limit boundary (count=9) - successfully adds 10th favorite")
        void atLimitBoundary_successfullyAddsTenthFavorite() {
            // Given
            String menuCode = "tenth-menu-item";
            when(userFavoriteRepository.existsByUserIdAndMenuCode(userId, menuCode)).thenReturn(false);
            when(userFavoriteRepository.countByUserId(userId)).thenReturn(9L);

            UserFavorite savedFavorite = new UserFavorite();
            savedFavorite.setId(UUID.randomUUID());
            savedFavorite.setUserId(userId);
            savedFavorite.setMenuCode(menuCode);
            savedFavorite.setOrderNumber(9);

            when(userFavoriteRepository.save(any(UserFavorite.class))).thenReturn(savedFavorite);

            // When
            UserFavorite result = userFavoriteService.addFavorite(userId, menuCode);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderNumber()).isEqualTo(9);

            verify(userFavoriteRepository).save(any(UserFavorite.class));
        }

        @Test
        @DisplayName("over limit (count=11) - throws IllegalStateException")
        void overLimit_throwsIllegalStateException() {
            // Given
            String menuCode = "over-limit-item";
            when(userFavoriteRepository.existsByUserIdAndMenuCode(userId, menuCode)).thenReturn(false);
            when(userFavoriteRepository.countByUserId(userId)).thenReturn(11L);

            // When / Then
            assertThatThrownBy(() -> userFavoriteService.addFavorite(userId, menuCode))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Maximum favorites limit reached");

            verify(userFavoriteRepository, never()).save(any());
        }
    }

    // =====================================================
    // removeFavorite tests
    // =====================================================

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavorite {

        @Test
        @DisplayName("existing favorite - calls delete on repository")
        void existingFavorite_callsDeleteOnRepository() {
            // Given / When
            userFavoriteService.removeFavorite(userId, "dashboard");

            // Then
            verify(userFavoriteRepository).deleteByUserIdAndMenuCode(userId, "dashboard");
        }

        @Test
        @DisplayName("non-existing favorite - still calls delete (idempotent)")
        void nonExistingFavorite_stillCallsDelete() {
            // Given - deleteByUserIdAndMenuCode is void, does nothing if not found

            // When
            userFavoriteService.removeFavorite(userId, "nonexistent-menu");

            // Then - should still call the repository (idempotent operation)
            verify(userFavoriteRepository).deleteByUserIdAndMenuCode(userId, "nonexistent-menu");
        }

        @Test
        @DisplayName("remove with specific user and menu - correct parameters passed")
        void removeWithSpecificParams_correctParametersPassed() {
            // Given
            UUID specificUserId = UUID.randomUUID();
            String menuCode = "registry-e-reestr";

            // When
            userFavoriteService.removeFavorite(specificUserId, menuCode);

            // Then
            verify(userFavoriteRepository).deleteByUserIdAndMenuCode(specificUserId, menuCode);
            verifyNoMoreInteractions(userFavoriteRepository);
        }
    }

    // =====================================================
    // reorderFavorites tests
    // =====================================================

    @Nested
    @DisplayName("reorderFavorites")
    class ReorderFavorites {

        @Test
        @DisplayName("reorders existing favorites with new order numbers")
        void reordersExistingFavoritesWithNewOrderNumbers() {
            // Given - swap dashboard (0->2) and rating (2->0)
            List<FavoriteReorderItem> items = List.of(
                    new FavoriteReorderItem("dashboard", 2),
                    new FavoriteReorderItem("registry-e-reestr", 1),
                    new FavoriteReorderItem("rating-university", 0)
            );

            when(userFavoriteRepository.findByUserIdAndMenuCode(userId, "dashboard"))
                    .thenReturn(Optional.of(dashboardFavorite));
            when(userFavoriteRepository.findByUserIdAndMenuCode(userId, "registry-e-reestr"))
                    .thenReturn(Optional.of(registryFavorite));
            when(userFavoriteRepository.findByUserIdAndMenuCode(userId, "rating-university"))
                    .thenReturn(Optional.of(ratingFavorite));

            when(userFavoriteRepository.save(any(UserFavorite.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            userFavoriteService.reorderFavorites(userId, items);

            // Then
            assertThat(dashboardFavorite.getOrderNumber()).isEqualTo(2);
            assertThat(registryFavorite.getOrderNumber()).isEqualTo(1);
            assertThat(ratingFavorite.getOrderNumber()).isEqualTo(0);

            verify(userFavoriteRepository, times(3)).findByUserIdAndMenuCode(eq(userId), anyString());
            verify(userFavoriteRepository, times(3)).save(any(UserFavorite.class));
        }

        @Test
        @DisplayName("skips non-existing menu codes without error")
        void skipsNonExistingMenuCodes() {
            // Given
            List<FavoriteReorderItem> items = List.of(
                    new FavoriteReorderItem("dashboard", 0),
                    new FavoriteReorderItem("nonexistent-menu", 1)
            );

            when(userFavoriteRepository.findByUserIdAndMenuCode(userId, "dashboard"))
                    .thenReturn(Optional.of(dashboardFavorite));
            when(userFavoriteRepository.findByUserIdAndMenuCode(userId, "nonexistent-menu"))
                    .thenReturn(Optional.empty());

            when(userFavoriteRepository.save(any(UserFavorite.class))).thenAnswer(inv -> inv.getArgument(0));

            // When - should not throw
            userFavoriteService.reorderFavorites(userId, items);

            // Then
            assertThat(dashboardFavorite.getOrderNumber()).isEqualTo(0);

            verify(userFavoriteRepository).findByUserIdAndMenuCode(userId, "dashboard");
            verify(userFavoriteRepository).findByUserIdAndMenuCode(userId, "nonexistent-menu");
            // Only one save (for dashboard), nonexistent is skipped
            verify(userFavoriteRepository, times(1)).save(any(UserFavorite.class));
        }

        @Test
        @DisplayName("empty items list - no repository interactions")
        void emptyItemsList_noRepositoryInteractions() {
            // Given
            List<FavoriteReorderItem> items = Collections.emptyList();

            // When
            userFavoriteService.reorderFavorites(userId, items);

            // Then
            verify(userFavoriteRepository, never()).findByUserIdAndMenuCode(any(), anyString());
            verify(userFavoriteRepository, never()).save(any());
        }

        @Test
        @DisplayName("single item reorder - updates order number correctly")
        void singleItemReorder_updatesOrderNumber() {
            // Given
            List<FavoriteReorderItem> items = List.of(
                    new FavoriteReorderItem("dashboard", 5)
            );

            when(userFavoriteRepository.findByUserIdAndMenuCode(userId, "dashboard"))
                    .thenReturn(Optional.of(dashboardFavorite));
            when(userFavoriteRepository.save(dashboardFavorite)).thenReturn(dashboardFavorite);

            // When
            userFavoriteService.reorderFavorites(userId, items);

            // Then
            assertThat(dashboardFavorite.getOrderNumber()).isEqualTo(5);

            verify(userFavoriteRepository).findByUserIdAndMenuCode(userId, "dashboard");
            verify(userFavoriteRepository).save(dashboardFavorite);
        }
    }
}
