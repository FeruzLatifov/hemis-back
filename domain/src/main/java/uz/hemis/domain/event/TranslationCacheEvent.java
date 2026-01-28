package uz.hemis.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Translation Cache Event
 *
 * <p>Published when translations are updated</p>
 * <p>Subscribed by all server instances to invalidate their cache</p>
 *
 * <p><strong>Use Case:</strong></p>
 * <ul>
 *   <li>Multi-server environment (horizontal scaling)</li>
 *   <li>Server A updates translation → publishes event</li>
 *   <li>Server B, C, D receive event → invalidate their cache</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationCacheEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Event type
     */
    private EventType type;

    /**
     * Message key (if single translation updated)
     */
    private String messageKey;

    /**
     * Language (if single language cache needs invalidation)
     */
    private String language;

    /**
     * Server ID that published the event
     */
    private String serverId;

    /**
     * Timestamp
     */
    private Instant timestamp;

    /**
     * Event Type
     */
    public enum EventType {
        /**
         * Single translation created
         */
        TRANSLATION_CREATED,

        /**
         * Single translation updated
         */
        TRANSLATION_UPDATED,

        /**
         * Single translation deleted
         */
        TRANSLATION_DELETED,

        /**
         * All caches should be cleared
         */
        CACHE_CLEAR_ALL,

        /**
         * Single language cache should be cleared
         */
        CACHE_CLEAR_LANGUAGE
    }
}
