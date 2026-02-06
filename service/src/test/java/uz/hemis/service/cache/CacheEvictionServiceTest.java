package uz.hemis.service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import uz.hemis.service.config.LanguageProperties;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CacheEvictionService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>evictUserMenu - cache eviction per user across all locales</li>
 *   <li>evictAllMenus - clearing entire menu cache</li>
 *   <li>evictAllCaches - nuclear option clearing all caches</li>
 *   <li>evictUserPermissions - permission cache eviction per user</li>
 *   <li>getCacheStats - cache statistics retrieval</li>
 *   <li>null cache handling - graceful handling when cache not found</li>
 * </ul>
 *
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CacheEvictionService Unit Tests")
class CacheEvictionServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private CacheVersionService cacheVersionService;

    @Mock
    private LanguageProperties languageProperties;

    @InjectMocks
    private CacheEvictionService cacheEvictionService;

    @Mock
    private Cache menuCache;

    @Mock
    private Cache permissionsCache;

    @Mock
    private Cache statsCache;

    // =====================================================
    // evictUserMenu tests
    // =====================================================

    @Nested
    @DisplayName("evictUserMenu")
    class EvictUserMenu {

        @Test
        @DisplayName("evicts menu cache for all supported locales for given user")
        void evictsAllLocales_forUser() {
            UUID userId = UUID.randomUUID();
            String[] locales = {"uz-UZ", "ru-RU", "en-US"};

            when(cacheManager.getCache("menu")).thenReturn(menuCache);
            when(languageProperties.getSupportedArray()).thenReturn(locales);

            cacheEvictionService.evictUserMenu(userId);

            verify(menuCache).evict(userId + ":uz-UZ");
            verify(menuCache).evict(userId + ":ru-RU");
            verify(menuCache).evict(userId + ":en-US");
            verify(menuCache, times(3)).evict(anyString());
        }

        @Test
        @DisplayName("handles null menu cache gracefully")
        void handlesNullCache() {
            UUID userId = UUID.randomUUID();
            when(cacheManager.getCache("menu")).thenReturn(null);

            // Should not throw
            cacheEvictionService.evictUserMenu(userId);

            verify(cacheManager).getCache("menu");
        }
    }

    // =====================================================
    // evictAllMenus tests
    // =====================================================

    @Nested
    @DisplayName("evictAllMenus")
    class EvictAllMenus {

        @Test
        @DisplayName("clears entire menu cache")
        void clearsEntireMenuCache() {
            when(cacheManager.getCache("menu")).thenReturn(menuCache);

            cacheEvictionService.evictAllMenus();

            verify(menuCache).clear();
        }

        @Test
        @DisplayName("handles null menu cache gracefully")
        void handlesNullCache() {
            when(cacheManager.getCache("menu")).thenReturn(null);

            // Should not throw
            cacheEvictionService.evictAllMenus();

            verify(cacheManager).getCache("menu");
        }
    }

    // =====================================================
    // evictAllCaches tests
    // =====================================================

    @Nested
    @DisplayName("evictAllCaches")
    class EvictAllCaches {

        @Test
        @DisplayName("clears all caches in cache manager")
        void clearsAllCaches() {
            Collection<String> cacheNames = List.of("menu", "userPermissions", "stats", "i18n");
            when(cacheManager.getCacheNames()).thenReturn(cacheNames);
            when(cacheManager.getCache("menu")).thenReturn(menuCache);
            when(cacheManager.getCache("userPermissions")).thenReturn(permissionsCache);
            when(cacheManager.getCache("stats")).thenReturn(statsCache);
            when(cacheManager.getCache("i18n")).thenReturn(mock(Cache.class));

            cacheEvictionService.evictAllCaches();

            verify(menuCache).clear();
            verify(permissionsCache).clear();
            verify(statsCache).clear();
        }

        @Test
        @DisplayName("handles empty cache names gracefully")
        void handlesEmptyCacheNames() {
            when(cacheManager.getCacheNames()).thenReturn(Collections.emptyList());

            // Should not throw
            cacheEvictionService.evictAllCaches();

            verify(cacheManager).getCacheNames();
        }
    }

    // =====================================================
    // evictUserPermissions tests
    // =====================================================

    @Nested
    @DisplayName("evictUserPermissions")
    class EvictUserPermissions {

        @Test
        @DisplayName("evicts permissions cache for specific user")
        void evictsForUser() {
            UUID userId = UUID.randomUUID();
            when(cacheManager.getCache("userPermissions")).thenReturn(permissionsCache);

            cacheEvictionService.evictUserPermissions(userId);

            verify(permissionsCache).evict(userId);
        }

        @Test
        @DisplayName("handles null permissions cache gracefully")
        void handlesNullCache() {
            UUID userId = UUID.randomUUID();
            when(cacheManager.getCache("userPermissions")).thenReturn(null);

            // Should not throw
            cacheEvictionService.evictUserPermissions(userId);

            verify(cacheManager).getCache("userPermissions");
        }
    }

    // =====================================================
    // getCacheStats tests
    // =====================================================

    @Nested
    @DisplayName("getCacheStats")
    class GetCacheStats {

        @Test
        @DisplayName("returns stats map with active caches and total count")
        void returnsStatsMap() {
            Collection<String> cacheNames = List.of("menu", "userPermissions", "stats");
            when(cacheManager.getCacheNames()).thenReturn(cacheNames);
            when(cacheManager.getCache("menu")).thenReturn(menuCache);
            when(cacheManager.getCache("userPermissions")).thenReturn(permissionsCache);
            when(cacheManager.getCache("stats")).thenReturn(statsCache);

            Map<String, Object> stats = cacheEvictionService.getCacheStats();

            assertThat(stats).isNotNull();
            assertThat(stats.get("menu")).isEqualTo("active");
            assertThat(stats.get("userPermissions")).isEqualTo("active");
            assertThat(stats.get("stats")).isEqualTo("active");
            assertThat(stats.get("totalCaches")).isEqualTo(3);
        }

        @Test
        @DisplayName("returns stats with zero caches when none registered")
        void returnsZeroCaches_whenNoneRegistered() {
            when(cacheManager.getCacheNames()).thenReturn(Collections.emptyList());

            Map<String, Object> stats = cacheEvictionService.getCacheStats();

            assertThat(stats).isNotNull();
            assertThat(stats.get("totalCaches")).isEqualTo(0);
        }
    }
}
