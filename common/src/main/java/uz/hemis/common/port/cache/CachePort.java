package uz.hemis.common.port.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Cache Port - Framework-agnostic cache abstraction
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This interface is defined in common module (inner layer)</li>
 *   <li>Implementation is in service module (outer layer)</li>
 *   <li>Services use this port for explicit cache operations</li>
 * </ul>
 *
 * <p><strong>Note:</strong> @Cacheable annotations remain in services
 * as they are declarative and AOP-based. This port is for explicit
 * cache operations like eviction and programmatic caching.</p>
 *
 * @since 2.0.0
 */
public interface CachePort {

    /**
     * Get value from cache
     *
     * @param cacheName cache name (e.g., "i18n", "menus")
     * @param key cache key
     * @param <T> value type
     * @return Optional containing cached value if present
     */
    <T> Optional<T> get(String cacheName, Object key);

    /**
     * Get value from cache or compute if absent
     *
     * @param cacheName cache name
     * @param key cache key
     * @param valueLoader supplier to compute value if not cached
     * @param <T> value type
     * @return cached or computed value
     */
    <T> T getOrCompute(String cacheName, Object key, Supplier<T> valueLoader);

    /**
     * Put value to cache
     *
     * @param cacheName cache name
     * @param key cache key
     * @param value value to cache
     */
    void put(String cacheName, Object key, Object value);

    /**
     * Evict single key from cache
     *
     * @param cacheName cache name
     * @param key cache key to evict
     */
    void evict(String cacheName, Object key);

    /**
     * Clear entire cache
     *
     * @param cacheName cache name to clear
     */
    void clear(String cacheName);

    /**
     * Check if key exists in cache
     *
     * @param cacheName cache name
     * @param key cache key
     * @return true if key exists
     */
    boolean exists(String cacheName, Object key);
}
