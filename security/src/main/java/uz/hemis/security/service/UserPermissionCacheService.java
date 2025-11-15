package uz.hemis.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Permission Cache Service - Redis-based
 *
 * <p><strong>Best Practice Architecture:</strong></p>
 * <pre>
 * JWT Token (MINIMAL):
 * {
 *   "iss": "hemis",
 *   "sub": "60885987-1b61-4247-94c7-dff348347f93",  ← userId (UUID)
 *   "exp": 1762727000,
 *   "username": "admin"  ← Optional (for frontend display only)
 * }
 *
 * Permissions (REDIS):
 * Key: "user:permissions:60885987-1b61-4247-94c7-dff348347f93"
 * Value: ["student:read", "student:create", "teacher:read", ...]
 * TTL: 1 hour
 * </pre>
 *
 * <p><strong>Permission Loading Pipeline:</strong></p>
 * <ol>
 *   <li>JWT decode → extract userId from 'sub' claim</li>
 *   <li>Check Redis: GET user:permissions:{userId}</li>
 *   <li>If cache HIT → return Set&lt;String&gt;</li>
 *   <li>If cache MISS → load from DB via UserRepository</li>
 *   <li>Save to Redis with TTL: 1 hour</li>
 *   <li>Return Set&lt;String&gt;</li>
 * </ol>
 *
 * <p><strong>Performance Benefits:</strong></p>
 * <ul>
 *   <li>JWT size: ~180 bytes (vs 2KB+ with permissions)</li>
 *   <li>Zero DB queries for cached users (99% hit rate)</li>
 *   <li>Fast permission updates (just evict cache)</li>
 *   <li>Horizontal scaling (Redis cluster)</li>
 * </ul>
 *
 * <p><strong>Cache Eviction:</strong></p>
 * <ul>
 *   <li>User role changed → evict cache</li>
 *   <li>Permission added/removed → evict cache</li>
 *   <li>User disabled → evict cache</li>
 *   <li>TTL expired (1 hour) → auto-refresh from DB</li>
 * </ul>
 *
 * <p><strong>Fallback Strategy:</strong></p>
 * <ul>
 *   <li>If Redis is down → direct DB query (no caching)</li>
 *   <li>Logs warning but continues working</li>
 *   <li>Auto-reconnect when Redis comes back</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Service
@Slf4j
public class UserPermissionCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final uz.hemis.domain.repository.UserRepository userRepository;

    public UserPermissionCacheService(
        RedisTemplate<String, Object> redisTemplate,
        uz.hemis.domain.repository.UserRepository userRepository
    ) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    /**
     * Cache TTL: 1 hour
     *
     * <p>Balance between:</p>
     * <ul>
     *   <li>Freshness: Permissions updated within 1 hour</li>
     *   <li>Performance: 99% cache hit rate</li>
     * </ul>
     */
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    /**
     * Redis key prefix for user permissions
     */
    private static final String KEY_PREFIX = "user:permissions:";

    /**
     * Get user permissions (from Redis cache or DB)
     *
     * <p><strong>Flow:</strong></p>
     * <ol>
     *   <li>Check Redis: GET user:permissions:{userId}</li>
     *   <li>If HIT → return cached Set&lt;String&gt;</li>
     *   <li>If MISS → load from DB → cache → return</li>
     * </ol>
     *
     * <p><strong>Fallback:</strong></p>
     * <p>If Redis fails → load directly from DB (no caching)</p>
     *
     * @param userId User ID (UUID) from JWT 'sub' claim
     * @return Set of permission codes (e.g., "student:read", "teacher:create")
     */
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissions(java.util.UUID userId) {
        if (userId == null) {
            log.warn("getUserPermissions called with null userId");
            return Set.of();
        }

        String cacheKey = KEY_PREFIX + userId.toString();

        try {
            // Try to get from Redis cache
            Object cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                log.debug("✅ Cache HIT for userId: {}", userId);

                // Convert to Set (Redis may return ArrayList due to JSON serialization)
                if (cached instanceof Set) {
                    return (Set<String>) cached;
                } else if (cached instanceof java.util.Collection) {
                    return new java.util.HashSet<>((java.util.Collection<String>) cached);
                }
            }

            // Cache MISS - load from database
            log.debug("⚠️ Cache MISS for userId: {} - loading from DB", userId);
            Set<String> permissions = loadPermissionsFromDatabase(userId);

            // Save to Redis cache
            cacheUserPermissions(userId, permissions);

            return permissions;

        } catch (Exception e) {
            // Redis error - fallback to DB (no caching)
            log.error("❌ Redis error for userId: {} - falling back to DB: {}", userId, e.getMessage());
            return loadPermissionsFromDatabase(userId);
        }
    }

    /**
     * Load permissions from database via UserRepository
     *
     * <p>Loads user by ID and extracts permissions from roles</p>
     *
     * @param userId User ID (UUID)
     * @return Set of permission codes
     */
    private Set<String> loadPermissionsFromDatabase(java.util.UUID userId) {
        try {
            // Load user with permissions eagerly (1 query with JOIN FETCH)
            uz.hemis.domain.entity.User user = userRepository.findByIdWithPermissions(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            Set<String> permissions = user.getAllPermissions().stream()
                    .map(uz.hemis.domain.entity.Permission::getCode)
                    .collect(Collectors.toSet());

            log.debug("Loaded {} permissions from DB for userId: {}", permissions.size(), userId);

            return permissions;

        } catch (Exception e) {
            log.error("Failed to load permissions from DB for userId: {}", userId, e);
            return Set.of();  // Empty permissions on error
        }
    }

    /**
     * Cache user permissions in Redis
     *
     * <p><strong>TTL:</strong> 1 hour</p>
     *
     * @param userId User ID (UUID)
     * @param permissions Set of permission codes
     */
    public void cacheUserPermissions(java.util.UUID userId, Set<String> permissions) {
        if (userId == null) {
            return;
        }

        String cacheKey = KEY_PREFIX + userId.toString();

        try {
            redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_TTL);
            log.debug("✅ Cached {} permissions for userId: {} (TTL: 1 hour)", permissions.size(), userId);

        } catch (Exception e) {
            log.warn("Failed to cache permissions for userId: {} - {}", userId, e.getMessage());
            // Continue without caching (not critical)
        }
    }

    /**
     * Evict user permission cache
     *
     * <p><strong>Use cases:</strong></p>
     * <ul>
     *   <li>User role changed</li>
     *   <li>Permission added/removed</li>
     *   <li>User disabled</li>
     * </ul>
     *
     * @param userId User ID (UUID)
     */
    public void evictUserCache(java.util.UUID userId) {
        if (userId == null) {
            return;
        }

        String cacheKey = KEY_PREFIX + userId.toString();

        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("✅ Evicted cache for userId: {}", userId);
            } else {
                log.debug("Cache key not found for userId: {}", userId);
            }

        } catch (Exception e) {
            log.warn("Failed to evict cache for userId: {} - {}", userId, e.getMessage());
        }
    }

    /**
     * Clear all user permission caches
     *
     * <p><strong>Use case:</strong> Mass permission update (e.g., system maintenance)</p>
     */
    public void clearAllCaches() {
        try {
            String pattern = KEY_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("✅ Cleared {} user permission caches", deleted);
            }

        } catch (Exception e) {
            log.error("Failed to clear all caches: {}", e.getMessage(), e);
        }
    }

    /**
     * Get cache statistics (for monitoring)
     *
     * @return Cache stats map
     */
    public java.util.Map<String, Object> getCacheStats() {
        try {
            String pattern = KEY_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);

            return java.util.Map.of(
                    "total_cached_users", keys != null ? keys.size() : 0,
                    "cache_key_pattern", pattern,
                    "cache_ttl_hours", CACHE_TTL.toHours()
            );

        } catch (Exception e) {
            log.error("Failed to get cache stats: {}", e.getMessage());
            return java.util.Map.of("error", e.getMessage());
        }
    }
}
