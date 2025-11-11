package uz.hemis.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import uz.hemis.service.event.TranslationCacheEventListener;

/**
 * Translation Cache Redis Pub/Sub Configuration
 *
 * <p>Configures Redis Pub/Sub for translation cache synchronization across multiple server instances</p>
 *
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Multi-server environment (horizontal scaling)</li>
 *   <li>Server A updates translation → publishes event</li>
 *   <li>Server B, C, D receive event → invalidate their cache</li>
 * </ul>
 *
 * <p><strong>Channel:</strong> translation-cache-events</p>
 *
 * <p><strong>Why separate from security/RedisConfig:</strong></p>
 * <ul>
 *   <li>Avoids circular dependency (security → service → security)</li>
 *   <li>Translation cache is service-layer concern, not security</li>
 *   <li>Listener needs I18nService which is in service module</li>
 * </ul>
 */
@Configuration
@Slf4j
public class TranslationCacheConfig {

    private static final String TRANSLATION_CACHE_CHANNEL = "translation-cache-events";

    /**
     * Redis Message Listener Container for Translation Cache Events
     *
     * <p>Subscribes to translation-cache-events channel</p>
     */
    @Bean
    public RedisMessageListenerContainer translationCacheListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter translationCacheListenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Subscribe to translation cache events channel
        container.addMessageListener(translationCacheListenerAdapter,
            new PatternTopic(TRANSLATION_CACHE_CHANNEL));

        log.info("✅ Translation Cache Redis Pub/Sub configured - channel: {}", TRANSLATION_CACHE_CHANNEL);

        return container;
    }

    /**
     * Message Listener Adapter for Translation Cache Events
     *
     * <p>Adapts TranslationCacheEventListener to MessageListener interface</p>
     */
    @Bean
    public MessageListenerAdapter translationCacheListenerAdapter(TranslationCacheEventListener listener) {
        return new MessageListenerAdapter(listener, "onMessage");
    }
}
