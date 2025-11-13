package uz.hemis.security.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cache Invalidation Listener - Enterprise Distributed Cache Management
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Listen to Redis Pub/Sub cache invalidation signals</li>
 *   <li>Clear L1 cache (JVM/Caffeine) when signal received</li>
 *   <li>Leader Election: Only 1 pod loads from database</li>
 *   <li>Other pods load from Redis (L2 cache)</li>
 * </ul>
 *
 * <p><strong>10 Pods Scenario:</strong></p>
 * <pre>
 * T+0ms:   Admin clicks button → POST /admin/cache/refresh
 * T+10ms:  Redis Pub/Sub: publish "cache:invalidate:all" "refresh-{timestamp}"
 * T+20ms:  All 10 pods receive signal (this listener)
 * T+25ms:  Leader election (POD-5 wins via Redis SETNX)
 * T+30ms:  POD-5 loads from DB → Redis (4 queries, 4000 rows)
 * T+100ms: POD-1,2,3,4,6,7,8,9,10 load from Redis → L1 cache
 * T+150ms: All 10 pods synchronized ✅
 * </pre>
 *
 * <p><strong>Leader Election Strategy:</strong></p>
 * <pre>
 * Redis SETNX cache:warmup:lock {pod-id} EX 10
 * - If successful → This pod is LEADER (load from DB)
 * - If failed → Wait for leader (load from Redis)
 * </pre>
 *
 * <p><strong>Database Load Comparison:</strong></p>
 * <ul>
 *   <li>Without Leader Election: 10 pods × 4 queries = 40 DB queries</li>
 *   <li>With Leader Election: 1 pod × 4 queries = 4 DB queries ✅</li>
 *   <li>Database load reduction: 90%</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationListener implements MessageListener {

    private final RedisMessageListenerContainer listenerContainer;
    private final CacheManager cacheManager;
    private final RedisTemplate<String, String> redisMessageTemplate;
    private final ApplicationContext applicationContext;

    @Value("${spring.application.name:hemis-backend}")
    private String applicationName;

    @Value("${HOSTNAME:unknown}")
    private String podName;

    /**
     * Register to Redis Pub/Sub channels on startup
     */
    @PostConstruct
    public void init() {
        // Subscribe to cache invalidation channels
        listenerContainer.addMessageListener(
                this,
                new PatternTopic("cache:invalidate:*")
        );

        log.info("📡 CacheInvalidationListener registered");
        log.info("🎯 Subscribed to channels: cache:invalidate:*");
        log.info("🏷️  Pod name: {}", podName);
        log.info("✅ Ready to receive cache invalidation signals from Redis Pub/Sub");
    }

    /**
     * Handle incoming Redis Pub/Sub messages
     *
     * <p>This method is called when ANY pod publishes to cache:invalidate:* channels</p>
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String payload = new String(message.getBody());

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📡 Cache invalidation signal received");
        log.info("   Channel: {}", channel);
        log.info("   Payload: {}", payload);
        log.info("   Pod: {}", podName);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            // Step 1: Clear L1 cache (JVM/Caffeine) - Barcha podlarda
            clearLocalCache(channel);

            // Step 2: Leader Election - Faqat 1 ta pod database ga boradi
            boolean isLeader = tryBecomeLeader();

            if (isLeader) {
                log.info("🏆 I am the LEADER - Loading from database...");
                warmupCacheFromDatabase();
            } else {
                log.info("⏳ Waiting for leader to finish warmup...");
                // Wait a bit for leader to populate Redis
                Thread.sleep(2000);

                log.info("📥 Loading from Redis (leader already populated)...");
                warmupCacheFromRedis();
            }

            log.info("✅ Cache refresh completed successfully on pod: {}", podName);

        } catch (Exception e) {
            log.error("❌ Failed to process cache invalidation signal", e);
        }
    }

    /**
     * Clear L1 Cache (JVM/Caffeine)
     *
     * <p>Runs on ALL 10 pods in parallel</p>
     */
    private void clearLocalCache(String channel) {
        log.info("🧹 Clearing L1 cache (JVM)...");

        if (channel.equals("cache:invalidate:all")) {
            // Clear all caches
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("   ✓ Cleared cache: {}", cacheName);
                }
            });

        } else if (channel.equals("cache:invalidate:i18n")) {
            // Clear only i18n cache
            clearCache("i18n");

        } else if (channel.equals("cache:invalidate:menu")) {
            // Clear only menu cache
            clearCache("menu");

        } else if (channel.equals("cache:invalidate:permissions")) {
            // Clear only permissions cache
            clearCache("userPermissions");
        }

        log.info("✅ L1 cache cleared on pod: {}", podName);
    }

    /**
     * Clear specific cache by name
     */
    private void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("   ✓ Cleared cache: {}", cacheName);
        } else {
            log.warn("   ⚠️  Cache not found: {}", cacheName);
        }
    }

    /**
     * Leader Election using Redis SETNX
     *
     * <p>Only 1 pod out of 10 will become leader</p>
     *
     * @return true if this pod is the leader
     */
    private boolean tryBecomeLeader() {
        String lockKey = "cache:warmup:lock";
        String lockValue = podName + "-" + System.currentTimeMillis();

        try {
            // Try to acquire lock (SETNX with TTL)
            Boolean acquired = redisMessageTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(30));

            if (Boolean.TRUE.equals(acquired)) {
                log.info("🏆 Leader election SUCCESS - I am the leader!");
                return true;
            } else {
                log.info("⏳ Leader election FAILED - Another pod is the leader");
                return false;
            }

        } catch (Exception e) {
            log.error("❌ Leader election failed", e);
            // Fallback: Assume NOT leader (load from Redis)
            return false;
        }
    }

    /**
     * Warmup Cache from Database (Leader Only)
     *
     * <p>Database queries: 4 (1 per language)</p>
     * <p>Rows loaded: 4000</p>
     */
    private void warmupCacheFromDatabase() {
        log.info("🔥 Warmup from DATABASE started (LEADER pod)...");
        long startTime = System.currentTimeMillis();

        try {
            // Get I18nService from ApplicationContext (lazy loading to avoid circular dependency)
            Object i18nService = applicationContext.getBean("i18nService");

            // Call warmupCacheFromDatabase() method using reflection
            Method warmupMethod = i18nService.getClass().getMethod("warmupCacheFromDatabase");
            warmupMethod.invoke(i18nService);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("✅ Warmup from DATABASE completed in {}ms", elapsed);

        } catch (Exception e) {
            log.error("❌ Warmup from database failed", e);
            // Don't throw - graceful degradation (cache will load on next request)
        }
    }

    /**
     * Warmup Cache from Redis (Non-Leader Pods)
     *
     * <p>Redis queries: 4000 GET operations (bulk)</p>
     * <p>Database queries: 0 ✅</p>
     */
    private void warmupCacheFromRedis() {
        log.info("📥 Warmup from REDIS started (non-leader pod)...");
        long startTime = System.currentTimeMillis();

        try {
            // Get I18nService from ApplicationContext (lazy loading to avoid circular dependency)
            Object i18nService = applicationContext.getBean("i18nService");

            // Call warmupCacheFromRedis() method using reflection
            Method warmupMethod = i18nService.getClass().getMethod("warmupCacheFromRedis");
            warmupMethod.invoke(i18nService);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("✅ Warmup from REDIS completed in {}ms", elapsed);

        } catch (Exception e) {
            log.error("❌ Warmup from Redis failed", e);
            // Don't throw - graceful degradation (cache will load on next request)
        }
    }
}
