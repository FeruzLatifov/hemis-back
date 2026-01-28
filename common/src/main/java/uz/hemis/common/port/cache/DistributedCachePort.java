package uz.hemis.common.port.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Distributed Cache Port - Redis-specific operations
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This interface is defined in common module (inner layer)</li>
 *   <li>Implementation uses Redis (outer layer)</li>
 *   <li>For operations that need explicit TTL or distributed features</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Token storage with TTL</li>
 *   <li>Rate limiting</li>
 *   <li>Distributed locks</li>
 *   <li>Pub/Sub for cache invalidation</li>
 * </ul>
 *
 * @since 2.0.0
 */
public interface DistributedCachePort {

    /**
     * Store value with TTL
     *
     * @param key cache key
     * @param value value to store
     * @param ttl time-to-live duration
     */
    void store(String key, Object value, Duration ttl);

    /**
     * Retrieve value
     *
     * @param key cache key
     * @param <T> value type
     * @return Optional containing value if present and not expired
     */
    <T> Optional<T> retrieve(String key);

    /**
     * Delete key
     *
     * @param key cache key
     * @return true if key was deleted
     */
    boolean delete(String key);

    /**
     * Delete multiple keys by pattern
     *
     * @param pattern key pattern (e.g., "user:permissions:*")
     * @return number of keys deleted
     */
    long deleteByPattern(String pattern);

    /**
     * Check if key exists
     *
     * @param key cache key
     * @return true if key exists
     */
    boolean hasKey(String key);

    /**
     * Get remaining TTL
     *
     * @param key cache key
     * @return Optional containing remaining TTL, empty if key doesn't exist
     */
    Optional<Duration> getTimeToLive(String key);

    /**
     * Find keys matching pattern
     *
     * @param pattern key pattern
     * @return set of matching keys
     */
    Set<String> findKeys(String pattern);

    /**
     * Increment counter
     *
     * @param key counter key
     * @param delta increment amount
     * @return new counter value
     */
    long increment(String key, long delta);

    /**
     * Publish message to channel (for cache invalidation)
     *
     * @param channel channel name
     * @param message message to publish
     */
    void publish(String channel, Object message);
}
