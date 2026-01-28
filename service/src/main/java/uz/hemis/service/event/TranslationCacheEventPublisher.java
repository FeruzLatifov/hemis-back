package uz.hemis.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.domain.event.TranslationCacheEvent;
import uz.hemis.service.cache.CacheEvictionService;

import java.time.Instant;

/**
 * Translation Cache Event Publisher
 *
 * <p>Publishes cache invalidation events to Redis Pub/Sub channel</p>
 * <p>Other server instances will receive and process these events</p>
 *
 * <p><strong>Channel:</strong> translation-cache-events</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * // After updating translation
 * eventPublisher.publishTranslationUpdated("menu.dashboard");
 *
 * // After clearing all caches
 * eventPublisher.publishCacheClearAll();
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationCacheEventPublisher {

    private static final String CHANNEL = "translation-cache-events";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheEvictionService cacheEvictionService;

    /**
     * Publish translation created event
     */
    public void publishTranslationCreated(String messageKey) {
        evictMenusIfNecessary(messageKey);
        publishEvent(TranslationCacheEvent.builder()
            .type(TranslationCacheEvent.EventType.TRANSLATION_CREATED)
            .messageKey(messageKey)
            .serverId(TranslationCacheEventListener.getServerId())
            .timestamp(Instant.now())
            .build());
    }

    /**
     * Publish translation updated event
     */
    public void publishTranslationUpdated(String messageKey) {
        evictMenusIfNecessary(messageKey);
        publishEvent(TranslationCacheEvent.builder()
            .type(TranslationCacheEvent.EventType.TRANSLATION_UPDATED)
            .messageKey(messageKey)
            .serverId(TranslationCacheEventListener.getServerId())
            .timestamp(Instant.now())
            .build());
    }

    /**
     * Publish translation deleted event
     */
    public void publishTranslationDeleted(String messageKey) {
        evictMenusIfNecessary(messageKey);
        publishEvent(TranslationCacheEvent.builder()
            .type(TranslationCacheEvent.EventType.TRANSLATION_DELETED)
            .messageKey(messageKey)
            .serverId(TranslationCacheEventListener.getServerId())
            .timestamp(Instant.now())
            .build());
    }

    /**
     * Publish cache clear all event
     */
    public void publishCacheClearAll() {
        cacheEvictionService.evictAllMenus();
        publishEvent(TranslationCacheEvent.builder()
            .type(TranslationCacheEvent.EventType.CACHE_CLEAR_ALL)
            .serverId(TranslationCacheEventListener.getServerId())
            .timestamp(Instant.now())
            .build());
    }

    /**
     * Publish cache clear for specific language
     */
    public void publishCacheClearLanguage(String language) {
        cacheEvictionService.evictAllMenus();
        publishEvent(TranslationCacheEvent.builder()
            .type(TranslationCacheEvent.EventType.CACHE_CLEAR_LANGUAGE)
            .language(language)
            .serverId(TranslationCacheEventListener.getServerId())
            .timestamp(Instant.now())
            .build());
    }

    private void evictMenusIfNecessary(String messageKey) {
        if (messageKey != null && messageKey.startsWith("menu.")) {
            cacheEvictionService.evictAllMenus();
        }
    }

    /**
     * Internal method to publish event to Redis channel
     */
    private void publishEvent(TranslationCacheEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(CHANNEL, json);
            log.info("📤 Published translation cache event: type={}, key={}, server={}",
                event.getType(), event.getMessageKey(), event.getServerId());
        } catch (Exception e) {
            log.error("Failed to publish translation cache event", e);
        }
    }
}
