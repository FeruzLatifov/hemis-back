package uz.hemis.service.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.event.UserPermissionsChangedEvent;
import uz.hemis.service.cache.CacheEvictionService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheEvictionEventListener - event-driven cache invalidation")
class CacheEvictionEventListenerTest {

    @Mock
    private CacheEvictionService cacheEvictionService;

    @InjectMocks
    private CacheEvictionEventListener listener;

    // =====================================================
    // handleUserPermissionsChanged
    // =====================================================

    @Nested
    @DisplayName("handleUserPermissionsChanged(event)")
    class HandleUserPermissionsChanged {

        @Test
        @DisplayName("should evict user permissions cache for the affected userId")
        void evictsUserPermissionsCache() {
            UUID userId = UUID.randomUUID();
            UserPermissionsChangedEvent event = buildEvent(userId, UserPermissionsChangedEvent.ChangeType.ROLE_ADDED);

            listener.handleUserPermissionsChanged(event);

            verify(cacheEvictionService).evictUserPermissions(userId);
        }

        @Test
        @DisplayName("should evict user menu cache for the affected userId")
        void evictsUserMenuCache() {
            UUID userId = UUID.randomUUID();
            UserPermissionsChangedEvent event = buildEvent(userId, UserPermissionsChangedEvent.ChangeType.ROLE_REMOVED);

            listener.handleUserPermissionsChanged(event);

            verify(cacheEvictionService).evictUserMenu(userId);
        }

        @Test
        @DisplayName("should not throw when cacheEvictionService throws exception")
        void doesNotThrowOnCacheEvictionFailure() {
            UUID userId = UUID.randomUUID();
            UserPermissionsChangedEvent event = buildEvent(userId, UserPermissionsChangedEvent.ChangeType.PERMISSION_GRANTED);

            doThrow(new RuntimeException("Redis connection failed"))
                    .when(cacheEvictionService).evictUserPermissions(userId);

            // Should not propagate exception
            listener.handleUserPermissionsChanged(event);
        }
    }

    // =====================================================
    // handleRolePermissionsModified
    // =====================================================

    @Nested
    @DisplayName("handleRolePermissionsModified(event)")
    class HandleRolePermissionsModified {

        @Test
        @DisplayName("should evict all permissions and menus when changeType is ROLE_MODIFIED with affectedRoleCode")
        void evictsAllCachesForRoleModified() {
            UUID userId = UUID.randomUUID();
            UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
                    .userId(userId)
                    .username("admin")
                    .changeType(UserPermissionsChangedEvent.ChangeType.ROLE_MODIFIED)
                    .affectedRoleCode("ROLE_TEACHER")
                    .previousRoles(List.of("ROLE_TEACHER"))
                    .newRoles(List.of("ROLE_TEACHER"))
                    .timestamp(Instant.now())
                    .changedBy("superadmin")
                    .build();

            listener.handleRolePermissionsModified(event);

            verify(cacheEvictionService).evictAllPermissions();
            verify(cacheEvictionService).evictAllMenus();
        }

        @Test
        @DisplayName("should skip processing when changeType is not ROLE_MODIFIED")
        void skipsWhenChangeTypeIsNotRoleModified() {
            UUID userId = UUID.randomUUID();
            UserPermissionsChangedEvent event = buildEvent(userId, UserPermissionsChangedEvent.ChangeType.ROLE_ADDED);

            listener.handleRolePermissionsModified(event);

            verifyNoInteractions(cacheEvictionService);
        }

        @Test
        @DisplayName("should skip processing when affectedRoleCode is null even if changeType is ROLE_MODIFIED")
        void skipsWhenAffectedRoleCodeIsNull() {
            UUID userId = UUID.randomUUID();
            UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
                    .userId(userId)
                    .username("admin")
                    .changeType(UserPermissionsChangedEvent.ChangeType.ROLE_MODIFIED)
                    .affectedRoleCode(null)
                    .timestamp(Instant.now())
                    .changedBy("superadmin")
                    .build();

            listener.handleRolePermissionsModified(event);

            verify(cacheEvictionService, never()).evictAllPermissions();
            verify(cacheEvictionService, never()).evictAllMenus();
        }

        @Test
        @DisplayName("should not throw when cacheEvictionService throws exception during role modification")
        void doesNotThrowOnCacheEvictionFailure() {
            UUID userId = UUID.randomUUID();
            UserPermissionsChangedEvent event = UserPermissionsChangedEvent.builder()
                    .userId(userId)
                    .username("admin")
                    .changeType(UserPermissionsChangedEvent.ChangeType.ROLE_MODIFIED)
                    .affectedRoleCode("ROLE_DEAN")
                    .timestamp(Instant.now())
                    .changedBy("superadmin")
                    .build();

            doThrow(new RuntimeException("Redis timeout"))
                    .when(cacheEvictionService).evictAllPermissions();

            // Should not propagate exception
            listener.handleRolePermissionsModified(event);
        }
    }

    // =====================================================
    // Helper
    // =====================================================

    private UserPermissionsChangedEvent buildEvent(UUID userId, UserPermissionsChangedEvent.ChangeType changeType) {
        return UserPermissionsChangedEvent.builder()
                .userId(userId)
                .username("testuser")
                .changeType(changeType)
                .previousRoles(List.of("ROLE_STUDENT"))
                .newRoles(List.of("ROLE_STUDENT", "ROLE_TEACHER"))
                .timestamp(Instant.now())
                .changedBy("admin")
                .build();
    }
}
