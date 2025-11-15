package uz.hemis.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import uz.hemis.service.I18nService;
import uz.hemis.service.cache.CacheEvictionService;
import uz.hemis.domain.event.TranslationCacheEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetAddress;

/**
 * Translation Cache Event Listener
 *
 * <p>Listens to Redis Pub/Sub channel for translation cache events</p>
 * <p>Invalidates local cache when events are received from other servers</p>
 *
 * <p><strong>Channel:</strong> translation-cache-events</p>
 *
 * <p><strong>Flow:</strong></p>
 * <ol>
 *   <li>Server A updates translation</li>
 *   <li>Server A publishes event to Redis channel</li>
 *   <li>Server B, C, D receive event via this listener</li>
 *   <li>Each server invalidates its local cache</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TranslationCacheEventListener implements MessageListener {

    private final I18nService i18nService;
    private final CacheEvictionService cacheEvictionService;
    private final ObjectMapper objectMapper;

    private static String SERVER_ID;

    static {
        try {
            SERVER_ID = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            SERVER_ID = "unknown-" + System.currentTimeMillis();
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Deserialize event
            String json = new String(message.getBody());
            TranslationCacheEvent event = objectMapper.readValue(json, TranslationCacheEvent.class);

            // Ignore events from this server (avoid double invalidation)
            if (SERVER_ID.equals(event.getServerId())) {
                log.debug("Ignoring event from same server: {}", event.getType());
                return;
            }

            log.info("📡 Received translation cache event from server {}: type={}, key={}, language={}",
                event.getServerId(), event.getType(), event.getMessageKey(), event.getLanguage());

            // Handle event based on type
            switch (event.getType()) {
                case TRANSLATION_CREATED:
                case TRANSLATION_UPDATED:
                case TRANSLATION_DELETED:
                    // Clear caches when a specific translation changed
                    i18nService.clearCache();
                    evictMenusIfNeeded(event.getMessageKey(), false);
                    log.info("✅ Cache invalidated due to event: {}", event.getType());
                    break;

                case CACHE_CLEAR_ALL:
                    // Clear all caches
                    i18nService.clearCache();
                    evictMenusIfNeeded(event.getMessageKey(), true);
                    log.info("✅ Cache invalidated due to event: {}", event.getType());
                    break;

                case CACHE_CLEAR_LANGUAGE:
                    // Clear specific language cache
                    if (event.getLanguage() != null) {
                        // For now, clear all caches (language-specific invalidation can be added later)
                        i18nService.clearCache();
                        cacheEvictionService.evictAllMenus();
                        log.info("✅ Cache invalidated for language: {}", event.getLanguage());
                    }
                    break;

                default:
                    log.warn("Unknown event type: {}", event.getType());
            }

        } catch (Exception e) {
            log.error("Error processing translation cache event", e);
        }
    }

    private void evictMenusIfNeeded(String messageKey, boolean force) {
        if (force) {
            cacheEvictionService.evictAllMenus();
            return;
        }
        if (messageKey != null && messageKey.startsWith("menu.")) {
            cacheEvictionService.evictAllMenus();
        }
    }

    public static String getServerId() {
        return SERVER_ID;
    }
}
