package uz.hemis.service.adapter.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import uz.hemis.common.port.cache.DistributedCachePort;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis Cache Adapter - Implements DistributedCachePort using RedisTemplate
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This adapter bridges DistributedCachePort (common) with Redis (infrastructure)</li>
 *   <li>Services depend on DistributedCachePort interface, not Redis directly</li>
 *   <li>Allows testing with mock implementations</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Token storage with TTL</li>
 *   <li>Rate limiting counters</li>
 *   <li>Distributed locks</li>
 *   <li>Pub/Sub for cache invalidation</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements DistributedCachePort {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void store(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Redis STORE: key={}, ttl={}", key, ttl);
        } catch (Exception e) {
            log.error("Redis STORE failed: key={}, error={}", key, e.getMessage());
            throw new RuntimeException("Redis store failed", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> retrieve(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Redis GET HIT: key={}", key);
                return Optional.of((T) value);
            }
            log.debug("Redis GET MISS: key={}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Redis GET failed: key={}, error={}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Redis DELETE: key={}, deleted={}", key, deleted);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("Redis DELETE failed: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                log.debug("Redis DELETE_PATTERN: pattern={}, deleted=0", pattern);
                return 0;
            }

            Long deleted = redisTemplate.delete(keys);
            log.debug("Redis DELETE_PATTERN: pattern={}, deleted={}", pattern, deleted);
            return deleted != null ? deleted : 0;
        } catch (Exception e) {
            log.error("Redis DELETE_PATTERN failed: pattern={}, error={}", pattern, e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Redis HAS_KEY failed: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Duration> getTimeToLive(String key) {
        try {
            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttlSeconds == null || ttlSeconds < 0) {
                return Optional.empty();
            }
            return Optional.of(Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Redis GET_TTL failed: key={}, error={}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Set<String> findKeys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys : new HashSet<>();
        } catch (Exception e) {
            log.error("Redis FIND_KEYS failed: pattern={}, error={}", pattern, e.getMessage());
            return new HashSet<>();
        }
    }

    @Override
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            log.debug("Redis INCREMENT: key={}, delta={}, result={}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis INCREMENT failed: key={}, delta={}, error={}", key, delta, e.getMessage());
            throw new RuntimeException("Redis increment failed", e);
        }
    }

    @Override
    public void publish(String channel, Object message) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("Redis PUBLISH: channel={}, message={}", channel, message);
        } catch (Exception e) {
            log.error("Redis PUBLISH failed: channel={}, error={}", channel, e.getMessage());
            throw new RuntimeException("Redis publish failed", e);
        }
    }
}
