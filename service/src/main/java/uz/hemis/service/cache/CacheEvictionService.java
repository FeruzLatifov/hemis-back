package uz.hemis.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Targeted Cache Eviction Service
 *
 * <p><strong>Enterprise Cache Invalidation Strategy:</strong></p>
 * <p>Instead of clearing ALL cache when data changes, this service provides
 * targeted eviction based on audit events:</p>
 *
 * <ul>
 *   <li><strong>User permissions changed</strong> → Evict only that user's menu cache</li>
 *   <li><strong>User role changed</strong> → Evict menu cache for all users with that role</li>
 *   <li><strong>Translation updated</strong> → Evict i18n cache for specific language</li>
 *   <li><strong>Menu structure changed</strong> → Evict all menu caches</li>
 *   <li><strong>Student data changed</strong> → Evict dashboard stats cache</li>
 * </ul>
 *
 * <p><strong>Performance Benefits:</strong></p>
 * <ul>
 *   <li>Minimal cache churn - only affected data is evicted</li>
 *   <li>Zero impact on unrelated users</li>
 *   <li>Faster cache regeneration (less data to reload)</li>
 * </ul>
 *
 * <p><strong>10 Pods Scenario:</strong></p>
 * <pre>
 * Admin updates user "john@hemis" permissions:
 *   1. CacheEvictionService.evictUserMenu("john@hemis")
 *   2. Redis Pub/Sub broadcasts to all 10 pods
 *   3. Each pod evicts L1 cache for key "menu:john@hemis:*"
 *   4. Other 99,999 users' caches remain intact ✅
 * </pre>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * // In UserService after permission update
 * userRepository.save(user);
 * cacheEvictionService.evictUserMenu(user.getUsername());
 * cacheEvictionService.evictUserPermissions(user.getId());
 *
 * // In TranslationService after translation update
 * translationRepository.save(translation);
 * cacheEvictionService.evictI18nLanguage(translation.getLanguage());
 *
 * // In StudentService after bulk import
 * studentRepository.saveAll(students);
 * cacheEvictionService.evictDashboardStats();
 * </pre>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheEvictionService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    // =====================================================
    // Menu Cache Eviction
    // =====================================================

    /**
     * Evict menu cache for specific user
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>User permissions changed</li>
     *   <li>User role changed</li>
     *   <li>User account disabled/enabled</li>
     * </ul>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Evicts ~4 keys (one per locale)</li>
     *   <li>Time: ~5ms (Redis DEL operations)</li>
     *   <li>Zero impact on other users ✅</li>
     * </ul>
     *
     * @param username Username (e.g., "admin", "john@hemis")
     */
    public void evictUserMenu(String username) {
        log.info("🗑️  Evicting menu cache for user: {}", username);

        org.springframework.cache.Cache menuCache = cacheManager.getCache("menu");
        if (menuCache == null) {
            log.warn("⚠️  Menu cache not found");
            return;
        }

        // Evict all locales for this user
        String[] locales = {"uz-UZ", "oz-UZ", "ru-RU", "en-US"};
        int evictedCount = 0;

        for (String locale : locales) {
            String cacheKey = username + ":" + locale;
            menuCache.evict(cacheKey);
            evictedCount++;
            log.debug("   Evicted: {}", cacheKey);
        }

        log.info("✅ Evicted {} menu cache entries for user: {}", evictedCount, username);
    }

    /**
     * Evict menu cache for specific user by userId
     *
     * @param userId User UUID
     */
    public void evictUserMenu(UUID userId) {
        log.info("🗑️  Evicting menu cache for userId: {}", userId);

        org.springframework.cache.Cache menuCache = cacheManager.getCache("menu");
        if (menuCache == null) {
            log.warn("⚠️  Menu cache not found");
            return;
        }

        // Evict all locales for this user
        String[] locales = {"uz-UZ", "oz-UZ", "ru-RU", "en-US"};
        int evictedCount = 0;

        for (String locale : locales) {
            String cacheKey = userId + ":" + locale;
            menuCache.evict(cacheKey);
            evictedCount++;
            log.debug("   Evicted: {}", cacheKey);
        }

        log.info("✅ Evicted {} menu cache entries for userId: {}", evictedCount, userId);
    }

    /**
     * Evict ALL menu caches
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Menu structure changed (MenuConfig updated)</li>
     *   <li>Permission hierarchy changed</li>
     *   <li>Global settings changed</li>
     * </ul>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Clears entire menu cache</li>
     *   <li>Time: ~50ms (clear operation)</li>
     *   <li>Affects all users temporarily</li>
     * </ul>
     */
    public void evictAllMenus() {
        log.info("🗑️  Evicting ALL menu caches");

        org.springframework.cache.Cache menuCache = cacheManager.getCache("menu");
        if (menuCache != null) {
            menuCache.clear();
            log.info("✅ All menu caches evicted");
        } else {
            log.warn("⚠️  Menu cache not found");
        }
    }

    // =====================================================
    // I18n Cache Eviction
    // =====================================================

    /**
     * Evict i18n cache for specific language
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Translation updated for specific language</li>
     *   <li>Admin edited Russian translations</li>
     * </ul>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Evicts 1 language cache (~1000 messages)</li>
     *   <li>Time: ~5ms</li>
     *   <li>Other languages unaffected ✅</li>
     * </ul>
     *
     * @param language Language code (e.g., "ru-RU", "uz-UZ")
     */
    public void evictI18nLanguage(String language) {
        log.info("🗑️  Evicting i18n cache for language: {}", language);

        String cacheKey = "i18n:messages:" + language;

        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("✅ I18n cache evicted for language: {}", language);
            } else {
                log.warn("⚠️  I18n cache key not found: {}", cacheKey);
            }
        } catch (Exception e) {
            log.error("❌ Failed to evict i18n cache for language: {}", language, e);
        }
    }

    /**
     * Evict ALL i18n caches
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Bulk translation import</li>
     *   <li>Translation structure changed</li>
     * </ul>
     */
    public void evictAllI18n() {
        log.info("🗑️  Evicting ALL i18n caches");

        try {
            Set<String> keys = redisTemplate.keys("i18n:messages:*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("✅ Evicted {} i18n cache keys", deleted);
            } else {
                log.info("ℹ️  No i18n cache keys found");
            }
        } catch (Exception e) {
            log.error("❌ Failed to evict all i18n caches", e);
        }
    }

    // =====================================================
    // User Permissions Cache Eviction
    // =====================================================

    /**
     * Evict user permissions cache for specific user
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>User role changed</li>
     *   <li>Permissions granted/revoked</li>
     * </ul>
     *
     * @param userId User UUID
     */
    public void evictUserPermissions(UUID userId) {
        log.info("🗑️  Evicting permissions cache for user: {}", userId);

        org.springframework.cache.Cache permCache = cacheManager.getCache("userPermissions");
        if (permCache != null) {
            permCache.evict(userId);
            log.info("✅ Permissions cache evicted for user: {}", userId);
        } else {
            log.warn("⚠️  UserPermissions cache not found");
        }
    }

    /**
     * Evict permissions cache for all users with specific role
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Role permissions changed</li>
     *   <li>Admin changed "TEACHER" role permissions</li>
     * </ul>
     *
     * <p><strong>NOTE:</strong></p>
     * This requires querying database for all users with the role,
     * then evicting each user's cache. For large user bases, consider
     * clearing entire cache instead.
     *
     * @param roleCode Role code (e.g., "ROLE_ADMIN", "ROLE_TEACHER")
     */
    public void evictPermissionsByRole(String roleCode) {
        log.info("🗑️  Evicting permissions cache for role: {}", roleCode);
        log.warn("⚠️  Role-based eviction requires user lookup - consider evictAllPermissions() for large user bases");

        // For now, clear all permissions cache
        // TODO: Implement targeted eviction if user count is reasonable
        evictAllPermissions();
    }

    /**
     * Evict ALL permissions caches
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Permission structure changed</li>
     *   <li>Role hierarchy updated</li>
     * </ul>
     */
    public void evictAllPermissions() {
        log.info("🗑️  Evicting ALL permissions caches");

        org.springframework.cache.Cache permCache = cacheManager.getCache("userPermissions");
        if (permCache != null) {
            permCache.clear();
            log.info("✅ All permissions caches evicted");
        } else {
            log.warn("⚠️  UserPermissions cache not found");
        }
    }

    // =====================================================
    // Dashboard Stats Cache Eviction
    // =====================================================

    /**
     * Evict dashboard statistics cache
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Student data imported/updated</li>
     *   <li>University data changed</li>
     *   <li>Enrollment status changed</li>
     * </ul>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Evicts 1 key (stats:all)</li>
     *   <li>Time: ~1ms</li>
     *   <li>Next request regenerates from database (~30s)</li>
     * </ul>
     */
    public void evictDashboardStats() {
        log.info("🗑️  Evicting dashboard stats cache");

        org.springframework.cache.Cache statsCache = cacheManager.getCache("stats");
        if (statsCache != null) {
            statsCache.evict("all");
            log.info("✅ Dashboard stats cache evicted");
        } else {
            log.warn("⚠️  Stats cache not found");
        }
    }

    // =====================================================
    // University Search Cache Eviction
    // =====================================================

    /**
     * Evict university search cache
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>University data updated</li>
     *   <li>University added/removed</li>
     * </ul>
     */
    public void evictUniversitySearch() {
        log.info("🗑️  Evicting university search cache");

        org.springframework.cache.Cache searchCache = cacheManager.getCache("universitiesSearch");
        if (searchCache != null) {
            searchCache.clear();
            log.info("✅ University search cache evicted");
        } else {
            log.warn("⚠️  University search cache not found");
        }
    }

    /**
     * Evict university dictionaries cache
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Reference data updated (regions, types, etc.)</li>
     * </ul>
     */
    public void evictUniversityDictionaries() {
        log.info("🗑️  Evicting university dictionaries cache");

        org.springframework.cache.Cache dictCache = cacheManager.getCache("universityDictionaries");
        if (dictCache != null) {
            dictCache.clear();
            log.info("✅ University dictionaries cache evicted");
        } else {
            log.warn("⚠️  University dictionaries cache not found");
        }
    }

    // =====================================================
    // Utility Methods
    // =====================================================

    /**
     * Evict ALL caches (nuclear option)
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>System maintenance</li>
     *   <li>Major data migration</li>
     *   <li>Emergency cache clear</li>
     * </ul>
     *
     * <p><strong>WARNING:</strong></p>
     * This clears ALL caches across all cache types. Use targeted
     * eviction methods whenever possible for better performance.
     */
    public void evictAllCaches() {
        log.warn("🗑️  EVICTING ALL CACHES (NUCLEAR OPTION)");

        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("   Cleared cache: {}", cacheName);
            }
        }

        log.warn("✅ ALL CACHES EVICTED - Performance may degrade temporarily");
    }

    /**
     * Get cache statistics for monitoring
     *
     * @return Cache stats map
     */
    public java.util.Map<String, Object> getCacheStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                stats.put(cacheName, "active");
            }
        }

        stats.put("totalCaches", cacheManager.getCacheNames().size());
        return stats;
    }
}
