package uz.hemis.service.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;

/**
 * Two-Level Cache Implementation
 *
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>L1: Caffeine (JVM memory, per-pod, ~1ms)</li>
 *   <li>L2: Redis (shared, distributed, ~50ms)</li>
 *   <li>L3: Database (source of truth, ~1000ms)</li>
 * </ul>
 *
 * <p><strong>Read Flow:</strong></p>
 * <pre>
 * 1. Check Caffeine (L1) → If HIT: return (1ms) ✅
 * 2. Check Redis (L2) → If HIT: populate L1, return (50ms)
 * 3. Call valueLoader (DB) → Populate L1 + L2, return (1000ms)
 * </pre>
 *
 * <p><strong>Write Flow (put):</strong></p>
 * <pre>
 * 1. Write to Caffeine (L1) - immediate
 * 2. Write to Redis (L2) - sync for consistency
 * </pre>
 *
 * <p><strong>10 Pods Scenario:</strong></p>
 * <ul>
 *   <li>POD-1 has L1 cache (Caffeine) - 1ms</li>
 *   <li>POD-2 misses L1 → Loads from Redis L2 → Populates L1</li>
 *   <li>All pods share Redis L2 (distributed)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Slf4j
public class TwoLevelCache extends AbstractValueAdaptingCache {

    private final String name;
    private final Cache<Object, Object> caffeineCache;           // L1
    private final org.springframework.cache.Cache redisCache;     // L2

    public TwoLevelCache(String name,
                         Cache<Object, Object> caffeineCache,
                         org.springframework.cache.Cache redisCache) {
        super(true);  // allowNullValues = true
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.redisCache = redisCache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return caffeineCache;
    }

    /**
     * Get value from 2-level cache
     *
     * <p>Flow: L1 Caffeine → L2 Redis → null</p>
     */
    @Override
    protected Object lookup(Object key) {
        // Step 1: Try L1 (Caffeine)
        Object value = caffeineCache.getIfPresent(key);
        if (value != null) {
            log.debug("✅ L1 HIT (Caffeine): cache={}, key={}", name, key);
            return value;
        }

        // Step 2: Try L2 (Redis)
        org.springframework.cache.Cache.ValueWrapper redisValue = redisCache.get(key);
        if (redisValue != null) {
            Object redisObj = redisValue.get();
            log.debug("✅ L2 HIT (Redis): cache={}, key={}, populating L1", name, key);

            // Populate L1 from L2
            caffeineCache.put(key, redisObj);
            return redisObj;
        }

        log.debug("❌ CACHE MISS (L1+L2): cache={}, key={}", name, key);
        return null;
    }

    /**
     * Get value with loader (for @Cacheable)
     *
     * <p>If cache miss, calls valueLoader (DB query)</p>
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        // Try cache first
        Object value = lookup(key);
        if (value != null) {
            return (T) fromStoreValue(value);
        }

        // Cache miss - load from DB
        try {
            log.debug("🔄 Loading from DB: cache={}, key={}", name, key);
            T loadedValue = valueLoader.call();

            // Populate both L1 and L2
            put(key, loadedValue);

            return loadedValue;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load value for key: " + key, e);
        }
    }

    /**
     * Put value to both L1 and L2
     */
    @Override
    public void put(Object key, Object value) {
        Object storeValue = toStoreValue(value);

        // Write to L1 (Caffeine) - immediate
        caffeineCache.put(key, storeValue);
        log.debug("💾 L1 PUT (Caffeine): cache={}, key={}", name, key);

        // Write to L2 (Redis) - sync for consistency
        redisCache.put(key, storeValue);
        log.debug("💾 L2 PUT (Redis): cache={}, key={}", name, key);
    }

    /**
     * Evict from both L1 and L2
     */
    @Override
    public void evict(Object key) {
        caffeineCache.invalidate(key);
        redisCache.evict(key);
        log.debug("🗑️  Evicted from L1+L2: cache={}, key={}", name, key);
    }

    /**
     * Clear both L1 and L2
     */
    @Override
    public void clear() {
        caffeineCache.invalidateAll();
        redisCache.clear();
        log.info("🧹 Cleared L1+L2: cache={}", name);
    }

    /**
     * Get cache statistics for monitoring
     *
     * <p><strong>Returns detailed metrics:</strong></p>
     * <ul>
     *   <li><strong>L1 (Caffeine):</strong> hitCount, missCount, hitRate, size, evictions</li>
     *   <li><strong>L2 (Redis):</strong> cache name and status</li>
     * </ul>
     *
     * <p><strong>Example Response:</strong></p>
     * <pre>
     * {
     *   "cacheName": "i18n",
     *   "L1_Caffeine": {
     *     "hitCount": 1234,
     *     "missCount": 56,
     *     "hitRate": "95.66%",
     *     "totalRequests": 1290,
     *     "size": 987,
     *     "evictionCount": 12
     *   },
     *   "L2_Redis": {
     *     "type": "Redis",
     *     "status": "active"
     *   }
     * }
     * </pre>
     *
     * @return Map containing L1 and L2 statistics
     */
    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // Cache name
        stats.put("cacheName", name);

        // L1 Caffeine statistics
        com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = caffeineCache.stats();

        java.util.Map<String, Object> l1Stats = new java.util.HashMap<>();
        l1Stats.put("hitCount", caffeineStats.hitCount());
        l1Stats.put("missCount", caffeineStats.missCount());

        // Calculate hit rate percentage
        long totalRequests = caffeineStats.hitCount() + caffeineStats.missCount();
        double hitRatePercent = totalRequests > 0
            ? (caffeineStats.hitCount() * 100.0 / totalRequests)
            : 0.0;
        l1Stats.put("hitRate", String.format("%.2f%%", hitRatePercent));
        l1Stats.put("totalRequests", totalRequests);

        l1Stats.put("loadSuccessCount", caffeineStats.loadSuccessCount());
        l1Stats.put("loadFailureCount", caffeineStats.loadFailureCount());
        l1Stats.put("evictionCount", caffeineStats.evictionCount());
        l1Stats.put("size", caffeineCache.estimatedSize());

        stats.put("L1_Caffeine", l1Stats);

        // L2 Redis statistics (basic info)
        java.util.Map<String, Object> l2Stats = new java.util.HashMap<>();
        l2Stats.put("type", "Redis");
        l2Stats.put("status", "active");
        l2Stats.put("cacheName", redisCache.getName());

        stats.put("L2_Redis", l2Stats);

        return stats;
    }
}
