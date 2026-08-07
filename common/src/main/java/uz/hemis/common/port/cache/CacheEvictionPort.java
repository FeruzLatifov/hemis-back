package uz.hemis.common.port.cache;

/**
 * Cache Eviction Port - Centralized cache eviction operations
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This interface is defined in common module (inner layer)</li>
 *   <li>Implementation handles L1 (Caffeine) + L2 (Redis) eviction</li>
 *   <li>Services use this for explicit cache invalidation</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>After CRUD operations that affect cached data</li>
 *   <li>Manual cache refresh requests</li>
 *   <li>Cross-pod cache invalidation</li>
 * </ul>
 *
 * @since 2.0.0
 */
public interface CacheEvictionPort {

    // =====================================================
    // Menu Cache Operations
    // =====================================================

    /**
     * Evict all menu caches (L1 + L2)
     */
    void evictAllMenus();

    /**
     * Evict menu cache for specific user
     *
     * @param userId user ID
     */
    void evictMenuForUser(String userId);

    // =====================================================
    // Permission Cache Operations
    // =====================================================

    /**
     * Evict all permission caches
     */
    void evictAllPermissions();

    /**
     * Evict permissions for specific user
     *
     * @param userId user ID
     */
    void evictPermissionsForUser(String userId);

    /**
     * Evict the cached OTM access-scope for a user. Call whenever the user's university, roles,
     * or enabled/locked status changes, so a moved/dismissed user's scope does not linger until TTL.
     *
     * @param userId user UUID (string form)
     */
    void evictScopeForUser(String userId);

    // =====================================================
    // Translation/i18n Cache Operations
    // =====================================================

    /**
     * Evict all translation caches
     */
    void evictAllTranslations();

    /**
     * Evict translations for specific language
     *
     * @param locale language code (e.g., "uz-UZ")
     */
    void evictTranslationsForLocale(String locale);

    // =====================================================
    // Generic Cache Operations
    // =====================================================

    /**
     * Evict specific cache by name
     *
     * @param cacheName cache name
     */
    void evictCache(String cacheName);

    /**
     * Evict all caches (nuclear option)
     */
    void evictAllCaches();

    /**
     * Get cache statistics
     *
     * @return map of cache statistics
     */
    java.util.Map<String, Object> getCacheStatistics();
}
