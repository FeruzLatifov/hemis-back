package uz.hemis.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Cache Version Management Service
 *
 * <p><strong>Purpose:</strong> Manage versioned cache keys for enterprise distributed cache</p>
 *
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Redis stores version counters: i18n:version, menu:version, etc.</li>
 *   <li>Each cache entry includes version: i18n:v{N}:messages:uz-UZ</li>
 *   <li>On update/delete → version++ → all old entries become invalid</li>
 *   <li>Zero manual key deletion (automatic expiration via TTL)</li>
 * </ul>
 *
 * <p><strong>Example Flow:</strong></p>
 * <pre>
 * Initial state:
 *   i18n:version = 1
 *   i18n:v1:messages:uz-UZ = "..."
 *   i18n:v1:messages:ru-RU = "..."
 *
 * Admin updates translation:
 *   1. version++ → i18n:version = 2
 *   2. Publish Redis Pub/Sub: "i18n.invalidate"
 *   3. All pods clear L1 cache
 *   4. Next request uses v2 keys:
 *      i18n:v2:messages:uz-UZ (cache miss → load from DB → save with v2)
 *   5. Old v1 keys expire after 30 min (TTL)
 * </pre>
 *
 * <p><strong>Performance Benefits:</strong></p>
 * <ul>
 *   <li>No thundering herd: Only version number changes, data remains cached</li>
 *   <li>Atomic operations: Redis INCR is atomic</li>
 *   <li>Zero downtime: Old version still available during refresh</li>
 *   <li>Lazy loading: New version loaded on-demand</li>
 * </ul>
 *
 * <p><strong>Cache Namespaces:</strong></p>
 * <ul>
 *   <li>i18n:version → Translation cache version</li>
 *   <li>menu:version → Menu cache version</li>
 *   <li>userPermissions:version → Permissions cache version</li>
 *   <li>stats:version → Statistics cache version</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@Slf4j
public class CacheVersionService {

    private final RedisTemplate<String, String> redisMessageTemplate;

    // Version key prefix
    private static final String VERSION_KEY_PREFIX = "cache:version:";

    // Lock key prefix for distributed locking
    private static final String LOCK_KEY_PREFIX = "cache:lock:";

    // Lock timeout: 10 seconds
    private static final long LOCK_TIMEOUT_SECONDS = 10L;

    // Pub/Sub channel pattern: cache:invalidate:{namespace}
    private static final String INVALIDATE_CHANNEL_PATTERN = "cache:invalidate:%s";

    public CacheVersionService(RedisTemplate<String, String> redisMessageTemplate) {
        this.redisMessageTemplate = redisMessageTemplate;
        log.info("✅ CacheVersionService initialized");
    }

    /**
     * Get current cache version for namespace
     *
     * <p>Returns current version number, initializes to 1 if not exists</p>
     *
     * @param namespace Cache namespace (e.g., "i18n", "menu")
     * @return Current version number
     */
    public long getCurrentVersion(String namespace) {
        String versionKey = VERSION_KEY_PREFIX + namespace;
        String version = redisMessageTemplate.opsForValue().get(versionKey);

        if (version == null) {
            // Initialize version to 1
            redisMessageTemplate.opsForValue().setIfAbsent(versionKey, "1");
            log.info("🔢 Initialized cache version: {} = 1", namespace);
            return 1L;
        }

        return Long.parseLong(version);
    }

    /**
     * Increment cache version (atomic)
     *
     * <p><strong>Use Case:</strong> When data changes (create/update/delete)</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * // Translation updated
     * long newVersion = cacheVersionService.incrementVersion("i18n");
     * log.info("Cache version incremented: i18n = {}", newVersion);
     * </pre>
     *
     * @param namespace Cache namespace (e.g., "i18n", "menu")
     * @return New version number
     */
    public long incrementVersion(String namespace) {
        String versionKey = VERSION_KEY_PREFIX + namespace;
        Long newVersion = redisMessageTemplate.opsForValue().increment(versionKey);

        if (newVersion == null) {
            // Fallback: initialize and retry
            redisMessageTemplate.opsForValue().setIfAbsent(versionKey, "1");
            newVersion = 1L;
        }

        log.info("🔄 Cache version incremented: {} = {}", namespace, newVersion);

        return newVersion;
    }

    /**
     * Increment version AND publish invalidation event (atomic)
     *
     * <p><strong>Distributed Cache Invalidation Flow:</strong></p>
     * <pre>
     * 1. version++ (Redis INCR)
     * 2. Publish Pub/Sub message (Redis PUBLISH)
     * 3. All 10 pods receive message → clear L1 (Caffeine)
     * 4. Next request loads with new version
     * </pre>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * // Admin updates translation
     * cacheVersionService.incrementVersionAndPublish("i18n");
     * // → i18n:version++ (e.g., 1 → 2)
     * // → PUBLISH cache:invalidate:i18n "v2-1234567890"
     * // → All pods clear L1 Caffeine cache
     * </pre>
     *
     * @param namespace Cache namespace (e.g., "i18n", "menu")
     * @return New version number
     */
    public long incrementVersionAndPublish(String namespace) {
        // 1️⃣ Increment version (atomic)
        long newVersion = incrementVersion(namespace);

        // 2️⃣ Publish invalidation event
        String channel = String.format(INVALIDATE_CHANNEL_PATTERN, namespace);
        String payload = String.format("v%d-%d", newVersion, System.currentTimeMillis());

        redisMessageTemplate.convertAndSend(channel, payload);
        log.info("📡 Published invalidation: channel={}, payload={}", channel, payload);

        return newVersion;
    }

    /**
     * Build versioned cache key
     *
     * <p><strong>Format:</strong> namespace:v{version}:{subKey}</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>i18n:v1:messages:uz-UZ</li>
     *   <li>i18n:v1:messages:ru-RU</li>
     *   <li>menu:v3:admin:uz-UZ</li>
     *   <li>userPermissions:v2:user123</li>
     * </ul>
     *
     * @param namespace Cache namespace (e.g., "i18n", "menu")
     * @param subKey Sub-key (e.g., "messages:uz-UZ", "admin")
     * @return Versioned cache key
     */
    public String buildVersionedKey(String namespace, String subKey) {
        long version = getCurrentVersion(namespace);
        return String.format("%s:v%d:%s", namespace, version, subKey);
    }

    /**
     * Acquire distributed lock with timeout
     *
     * <p><strong>Purpose:</strong> Prevent thundering herd during cache refresh</p>
     *
     * <p><strong>Use Case:</strong></p>
     * <pre>
     * // 10 pods simultaneously miss cache
     * // Only 1 pod should query DB and populate cache
     * // Other 9 pods wait and read from cache
     *
     * if (cacheVersionService.acquireLock("i18n:warmup")) {
     *     try {
     *         // This pod won the race - load from DB
     *         loadFromDatabaseAndCache();
     *     } finally {
     *         cacheVersionService.releaseLock("i18n:warmup");
     *     }
     * } else {
     *     // Another pod is loading - wait and retry
     *     Thread.sleep(100);
     *     return getFromCache(); // Should be populated by winner pod
     * }
     * </pre>
     *
     * @param lockKey Lock identifier (e.g., "i18n:warmup")
     * @return true if lock acquired, false if already locked
     */
    public boolean acquireLock(String lockKey) {
        String fullLockKey = LOCK_KEY_PREFIX + lockKey;
        String lockValue = String.valueOf(System.currentTimeMillis());

        Boolean acquired = redisMessageTemplate.opsForValue()
                .setIfAbsent(fullLockKey, lockValue, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(acquired)) {
            log.debug("🔒 Lock acquired: {}", lockKey);
            return true;
        } else {
            log.debug("⏳ Lock already held: {}", lockKey);
            return false;
        }
    }

    /**
     * Release distributed lock
     *
     * @param lockKey Lock identifier
     */
    public void releaseLock(String lockKey) {
        String fullLockKey = LOCK_KEY_PREFIX + lockKey;
        redisMessageTemplate.delete(fullLockKey);
        log.debug("🔓 Lock released: {}", lockKey);
    }

    /**
     * Reset cache version (use with caution)
     *
     * <p><strong>WARNING:</strong> Resets version to 1, invalidates all cached data</p>
     *
     * <p><strong>Use Case:</strong> Manual cache clear during maintenance</p>
     *
     * @param namespace Cache namespace
     */
    public void resetVersion(String namespace) {
        String versionKey = VERSION_KEY_PREFIX + namespace;
        redisMessageTemplate.opsForValue().set(versionKey, "1");

        // Publish invalidation
        String channel = String.format(INVALIDATE_CHANNEL_PATTERN, namespace);
        String payload = String.format("reset-%d", System.currentTimeMillis());
        redisMessageTemplate.convertAndSend(channel, payload);

        log.warn("⚠️ Cache version reset: {} = 1", namespace);
    }

    /**
     * Get all cache versions (for monitoring)
     *
     * <p>Returns version numbers for all namespaces</p>
     *
     * @return Map of namespace → version
     */
    public java.util.Map<String, Long> getAllVersions() {
        java.util.Map<String, Long> versions = new java.util.HashMap<>();

        String[] namespaces = {"i18n", "menu", "userPermissions", "stats", "universitiesSearch", "universityDictionaries"};

        for (String namespace : namespaces) {
            versions.put(namespace, getCurrentVersion(namespace));
        }

        return versions;
    }
}
