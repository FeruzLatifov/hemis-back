package uz.hemis.service.adapter.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.hemis.common.port.cache.CacheEvictionPort;
import uz.hemis.service.cache.CacheEvictionService;
import uz.hemis.service.cache.TwoLevelCacheManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cache Eviction Adapter - Implements CacheEvictionPort using CacheEvictionService
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This adapter bridges CacheEvictionPort (common) with CacheEvictionService (infrastructure)</li>
 *   <li>Services depend on CacheEvictionPort interface, not Spring directly</li>
 *   <li>Handles L1 (Caffeine) + L2 (Redis) eviction transparently</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>After CRUD operations that affect cached data</li>
 *   <li>Manual cache refresh requests</li>
 *   <li>Cross-pod cache invalidation via Pub/Sub</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheEvictionAdapter implements CacheEvictionPort {

    private final CacheEvictionService cacheEvictionService;
    private final org.springframework.cache.CacheManager cacheManager;

    // =====================================================
    // Menu Cache Operations
    // =====================================================

    @Override
    public void evictAllMenus() {
        log.info("CacheEvictionPort: evictAllMenus()");
        cacheEvictionService.evictAllMenus();
    }

    @Override
    public void evictMenuForUser(String userId) {
        log.info("CacheEvictionPort: evictMenuForUser({})", userId);
        try {
            UUID uuid = UUID.fromString(userId);
            cacheEvictionService.evictUserMenu(uuid);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid userId format: {}", userId);
        }
    }

    // =====================================================
    // Permission Cache Operations
    // =====================================================

    @Override
    public void evictAllPermissions() {
        log.info("CacheEvictionPort: evictAllPermissions()");
        cacheEvictionService.evictAllPermissions();
    }

    @Override
    public void evictPermissionsForUser(String userId) {
        log.info("CacheEvictionPort: evictPermissionsForUser({})", userId);
        try {
            UUID uuid = UUID.fromString(userId);
            cacheEvictionService.evictUserPermissions(uuid);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid userId format: {}", userId);
        }
    }

    // =====================================================
    // Translation/i18n Cache Operations
    // =====================================================

    @Override
    public void evictAllTranslations() {
        log.info("CacheEvictionPort: evictAllTranslations()");
        cacheEvictionService.evictAllI18n();
    }

    @Override
    public void evictTranslationsForLocale(String locale) {
        log.info("CacheEvictionPort: evictTranslationsForLocale({})", locale);
        cacheEvictionService.evictI18nLanguage(locale);
    }

    // =====================================================
    // Generic Cache Operations
    // =====================================================

    @Override
    public void evictCache(String cacheName) {
        log.info("CacheEvictionPort: evictCache({})", cacheName);
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache cleared: {}", cacheName);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }

    @Override
    public void evictAllCaches() {
        log.warn("CacheEvictionPort: evictAllCaches() (NUCLEAR OPTION)");
        cacheEvictionService.evictAllCaches();
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        log.debug("CacheEvictionPort: getCacheStatistics()");

        Map<String, Object> stats = new HashMap<>();

        // Get basic stats from CacheEvictionService
        stats.putAll(cacheEvictionService.getCacheStats());

        // Add detailed stats if TwoLevelCacheManager is available
        if (cacheManager instanceof TwoLevelCacheManager) {
            TwoLevelCacheManager twoLevelCacheManager = (TwoLevelCacheManager) cacheManager;
            stats.put("detailedStats", twoLevelCacheManager.getCacheStatistics());
        }

        return stats;
    }
}
