package uz.hemis.service.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Two-Level Cache Manager
 *
 * <p><strong>Enterprise Distributed Cache Strategy:</strong></p>
 * <ul>
 *   <li>L1: Caffeine (JVM memory, per-pod, ~1ms) - 1000 entries, 10 min TTL</li>
 *   <li>L2: Redis (shared, distributed, ~50ms) - 60 min TTL</li>
 *   <li>L3: Database (PostgreSQL, ~1000ms) - on cache miss</li>
 * </ul>
 *
 * <p><strong>Performance Benefits:</strong></p>
 * <ul>
 *   <li>First request (same pod): 1000ms (DB) → 1ms (L1) = 1000x faster</li>
 *   <li>First request (other pod): 1000ms (DB) → 50ms (L2) = 20x faster</li>
 *   <li>Cached request (same pod): ~1ms (L1 Caffeine)</li>
 *   <li>Cached request (other pod): ~50ms (L2 Redis) → then ~1ms (L1)</li>
 * </ul>
 *
 * <p><strong>Cache Configuration:</strong></p>
 * <pre>
 * menu:            L1=1000 entries/10min, L2=60min
 * i18n:            L1=5000 entries/10min, L2=60min
 * userPermissions: L1=1000 entries/10min, L2=60min
 * stats:           L1=100 entries/10min,  L2=30min
 * </pre>
 *
 * @since 2.0.0
 */
@Slf4j
public class TwoLevelCacheManager implements CacheManager {

    private final Map<String, TwoLevelCache> cacheMap = new ConcurrentHashMap<>();
    private final org.springframework.data.redis.cache.RedisCacheManager redisCacheManager;

    // Caffeine L1 configuration - Per-Cache Size Optimization
    private static final long CAFFEINE_TTL_MINUTES = 30L;

    /**
     * Per-cache Caffeine L1 size configuration
     * <p>Optimized based on data volume and access patterns:</p>
     * <ul>
     *   <li><strong>i18n:</strong> 5000 entries - High volume (4 langs × 1000+ keys)</li>
     *   <li><strong>menu:</strong> 1000 entries - Medium volume (users × locales)</li>
     *   <li><strong>userPermissions:</strong> 1000 entries - Medium volume</li>
     *   <li><strong>stats:</strong> 100 entries - Low volume (aggregated data)</li>
     * </ul>
     */
    private static final Map<String, Long> CACHE_MAX_SIZES = Map.of(
        "i18n", 5000L,              // Translation cache - High volume
        "menu", 1000L,              // User menu cache
        "userPermissions", 1000L,   // User permissions cache
        "stats", 100L,              // Dashboard statistics
        "universitiesSearch", 500L, // University search results
        "universityDictionaries", 200L // Static reference data
    );

    private static final long DEFAULT_CAFFEINE_MAX_SIZE = 1000L;

    public TwoLevelCacheManager(
            org.springframework.data.redis.cache.RedisCacheManager redisCacheManager) {
        this.redisCacheManager = redisCacheManager;

        log.info("🚀 TwoLevelCacheManager initialized with per-cache size optimization");
        log.info("   L1 (Caffeine) sizes:");
        log.info("      - i18n: 5000 entries (translations)");
        log.info("      - menu: 1000 entries (user menus)");
        log.info("      - userPermissions: 1000 entries");
        log.info("      - stats: 100 entries (aggregated)");
        log.info("      - default: {} entries", DEFAULT_CAFFEINE_MAX_SIZE);
        log.info("   L1 TTL: {} minutes", CAFFEINE_TTL_MINUTES);
        log.info("   L2 (Redis): TTL 30 minutes (unified)");
    }

    @Override
    public org.springframework.cache.Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, this::createTwoLevelCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }

    /**
     * Create 2-level cache (Caffeine + Redis)
     */
    private TwoLevelCache createTwoLevelCache(String name) {
        long maxSize = getCaffeineMaxSize(name);
        log.info("📦 Creating 2-level cache: {} (L1 size: {} entries)", name, maxSize);

        // L1: Caffeine cache (JVM memory) - Per-cache size optimization
        Cache<Object, Object> caffeineCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(CAFFEINE_TTL_MINUTES, TimeUnit.MINUTES)
                .recordStats()  // Enable statistics for monitoring
                .build();

        // L2: Redis cache (distributed) - get from RedisCacheManager
        // May be wrapped in TransactionAwareCacheDecorator if transactionAware() enabled
        org.springframework.cache.Cache redisCache = redisCacheManager.getCache(name);

        if (redisCache == null) {
            throw new IllegalStateException("Redis cache not found for: " + name);
        }

        log.info("✅ 2-level cache created: {} (L1={} entries, L2=Redis)", name, maxSize);
        return new TwoLevelCache(name, caffeineCache, redisCache);
    }

    /**
     * Get Caffeine L1 maximum size for specific cache
     * <p>Per-cache optimization based on data volume:</p>
     * <ul>
     *   <li>i18n: 5000 entries (high volume)</li>
     *   <li>menu: 1000 entries (medium volume)</li>
     *   <li>stats: 100 entries (low volume)</li>
     *   <li>default: 1000 entries</li>
     * </ul>
     *
     * @param cacheName Cache name
     * @return Maximum number of entries for L1 Caffeine cache
     */
    private long getCaffeineMaxSize(String cacheName) {
        return CACHE_MAX_SIZES.getOrDefault(cacheName, DEFAULT_CAFFEINE_MAX_SIZE);
    }

    /**
     * Get cache statistics for all caches
     * <p>Returns detailed metrics for monitoring and debugging</p>
     *
     * @return Map of cache name → statistics
     */
    public Map<String, Map<String, Object>> getCacheStatistics() {
        Map<String, Map<String, Object>> allStats = new java.util.HashMap<>();

        for (Map.Entry<String, TwoLevelCache> entry : cacheMap.entrySet()) {
            String cacheName = entry.getKey();
            TwoLevelCache cache = entry.getValue();

            Map<String, Object> stats = cache.getStatistics();
            allStats.put(cacheName, stats);
        }

        return allStats;
    }
}
