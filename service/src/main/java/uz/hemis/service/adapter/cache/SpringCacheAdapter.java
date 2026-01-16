package uz.hemis.service.adapter.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import uz.hemis.common.port.cache.CachePort;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Spring Cache Adapter - Implements CachePort using Spring CacheManager
 *
 * <p><strong>Clean Architecture:</strong></p>
 * <ul>
 *   <li>This adapter bridges CachePort (domain/common) with Spring CacheManager (infrastructure)</li>
 *   <li>Services depend on CachePort interface, not Spring directly</li>
 *   <li>Allows testing with mock implementations</li>
 * </ul>
 *
 * <p><strong>Two-Level Cache:</strong></p>
 * <ul>
 *   <li>L1: Caffeine (JVM memory, ~1ms)</li>
 *   <li>L2: Redis (distributed, ~50ms)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringCacheAdapter implements CachePort {

    private final CacheManager cacheManager;

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: {}", cacheName);
            return Optional.empty();
        }

        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper == null) {
            log.debug("Cache MISS: cache={}, key={}", cacheName, key);
            return Optional.empty();
        }

        log.debug("Cache HIT: cache={}, key={}", cacheName, key);
        return Optional.ofNullable((T) valueWrapper.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrCompute(String cacheName, Object key, Supplier<T> valueLoader) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: {} - computing value directly", cacheName);
            return valueLoader.get();
        }

        // Try to get from cache first
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper != null) {
            log.debug("Cache HIT: cache={}, key={}", cacheName, key);
            return (T) valueWrapper.get();
        }

        // Cache miss - compute and store
        log.debug("Cache MISS: cache={}, key={} - computing value", cacheName, key);
        T value = valueLoader.get();
        cache.put(key, value);
        return value;
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: {} - cannot store value", cacheName);
            return;
        }

        cache.put(key, value);
        log.debug("Cache PUT: cache={}, key={}", cacheName, key);
    }

    @Override
    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: {} - cannot evict", cacheName);
            return;
        }

        cache.evict(key);
        log.debug("Cache EVICT: cache={}, key={}", cacheName, key);
    }

    @Override
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: {} - cannot clear", cacheName);
            return;
        }

        cache.clear();
        log.info("Cache CLEARED: cache={}", cacheName);
    }

    @Override
    public boolean exists(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return false;
        }

        return cache.get(key) != null;
    }
}
