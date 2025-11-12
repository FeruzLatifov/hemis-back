package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.SystemMessage;
import uz.hemis.domain.entity.SystemMessageTranslation;
import uz.hemis.domain.repository.SystemMessageRepository;
import uz.hemis.domain.repository.SystemMessageTranslationRepository;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * I18n (Internationalization) Service - UNIVER Pattern Implementation
 *
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Redis Cache Layer (1-day TTL) - Zero DB load for cached data</li>
 *   <li>Database Fallback - When cache miss or Redis unavailable</li>
 *   <li>UNIVER Fallback Logic - language-region → language → default (uz)</li>
 * </ul>
 *
 * <p><strong>Performance Strategy:</strong></p>
 * <ul>
 *   <li>Bulk Loading - All translations loaded at once per language</li>
 *   <li>Redis Key Pattern: "i18n:messages:{language}" → Map&lt;String, String&gt;</li>
 *   <li>Startup Warmup - Cache populated for main languages (uz-UZ, ru-RU, en-US)</li>
 *   <li>Zero Database Load - After warmup, all requests served from Redis</li>
 * </ul>
 *
 * <p><strong>UNIVER Fallback Sequence:</strong></p>
 * <ol>
 *   <li>Try exact language match (e.g., ru-RU)</li>
 *   <li>Try language without region (e.g., ru)</li>
 *   <li>Return default message (Uzbek)</li>
 * </ol>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Get single message
 * String saveButton = i18nService.getMessage("button.save", "ru-RU");
 * // Returns: "Сохранить"
 *
 * // Get all messages for language (frontend bulk load)
 * Map&lt;String, String&gt; allMessages = i18nService.getAllMessages("en-US");
 * // Returns: {"button.save": "Save", "button.cancel": "Cancel", ...}
 *
 * // Invalidate cache after update
 * i18nService.invalidateCache("ru-RU");
 * </pre>
 *
 * @see SystemMessage
 * @see SystemMessageTranslation
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class I18nService {

    private final SystemMessageRepository systemMessageRepository;
    private final SystemMessageTranslationRepository translationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // =====================================================
    // Constants
    // =====================================================

    /**
     * Redis key prefix for translation cache
     * <p>Pattern: i18n:messages:{language}</p>
     */
    private static final String CACHE_KEY_PREFIX = "i18n:messages:";

    /**
     * Cache TTL - 1 day
     * <p>Translations rarely change, so long TTL is acceptable</p>
     */
    private static final Duration CACHE_TTL = Duration.ofDays(1);

    /**
     * Main languages for startup warmup
     * <p>Cache populated for these languages at application startup</p>
     */
    private static final List<String> MAIN_LANGUAGES = Arrays.asList(
        "uz-UZ",  // O'zbek (lotin)
        "oz-UZ",  // Ўзбек (kirill)
        "ru-RU",  // Русский
        "en-US"   // English
    );

    /**
     * Properties file translations cache (3rd fallback)
     * <p>Loaded from i18n/menu_{lang}.properties files</p>
     * <p>Pattern: {language -> {messageKey -> translation}}</p>
     */
    private final Map<String, Properties> propertiesCache = new ConcurrentHashMap<>();

    /**
     * Default language fallback
     * <p>When no translation found, use Uzbek (Latin)</p>
     */
    private static final String DEFAULT_LANGUAGE = "uz-UZ";

    // =====================================================
    // Startup Warmup
    // =====================================================

    /**
     * Warmup cache at application startup
     * <p>Pre-loads main languages into Redis for instant availability</p>
     *
     * <p><strong>Benefits:</strong></p>
     * <ul>
     *   <li>Zero database queries after startup for main languages</li>
     *   <li>Instant response time for first requests</li>
     *   <li>Reduced database connection pool usage</li>
     * </ul>
     */
    @PostConstruct
    public void warmupCache() {
        log.info("🔥 Starting I18n cache warmup for languages: {}", MAIN_LANGUAGES);

        // Load properties files first (default fallback)
        loadPropertiesFiles();

        for (String language : MAIN_LANGUAGES) {
            try {
                Map<String, String> messages = loadFromDatabaseBulk(language);
                cacheMessages(language, messages);
                log.info("✅ Cached {} messages for language: {}", messages.size(), language);
            } catch (Exception e) {
                log.error("❌ Failed to warmup cache for language: {}", language, e);
            }
        }

        log.info("🎉 I18n cache warmup completed");
    }

    /**
     * Load properties files for all languages (3rd fallback)
     * <p>Files: i18n/menu_uz.properties, i18n/menu_oz.properties, etc.</p>
     */
    private void loadPropertiesFiles() {
        Map<String, String> languageFiles = Map.of(
            "uz-UZ", "i18n/menu_uz.properties",
            "oz-UZ", "i18n/menu_oz.properties",
            "ru-RU", "i18n/menu_ru.properties",
            "en-US", "i18n/menu_en.properties"
        );

        for (Map.Entry<String, String> entry : languageFiles.entrySet()) {
            String language = entry.getKey();
            String filePath = entry.getValue();

            try {
                ClassPathResource resource = new ClassPathResource(filePath);
                if (resource.exists()) {
                    // ✅ Load properties with UTF-8 encoding (for Cyrillic support)
                    Properties props = new Properties();
                    try (java.io.InputStreamReader reader = new java.io.InputStreamReader(
                            resource.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
                        props.load(reader);
                    }
                    propertiesCache.put(language, props);
                    log.info("✅ Loaded {} properties for language: {} from {} (UTF-8)",
                        props.size(), language, filePath);
                } else {
                    log.warn("⚠️  Properties file not found: {}", filePath);
                }
            } catch (IOException e) {
                log.error("❌ Failed to load properties file: {}", filePath, e);
            }
        }
    }

    // =====================================================
    // Public API Methods
    // =====================================================

    /**
     * Get message by key for specific language
     * <p>UNIVER fallback logic applied</p>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Cache Hit: O(1) - instant Redis lookup</li>
     *   <li>Cache Miss: O(n) - database query + cache update</li>
     * </ul>
     *
     * <p><strong>Fallback Sequence:</strong></p>
     * <ol>
     *   <li>Check cache for exact language (ru-RU)</li>
     *   <li>If not found, try language prefix (ru)</li>
     *   <li>If still not found, return default Uzbek message</li>
     * </ol>
     *
     * @param messageKey Message key (e.g., "button.save")
     * @param language Language code (e.g., "ru-RU")
     * @return Translated message (never null)
     */
    public String getMessage(String messageKey, String language) {
        log.debug("Getting message: key={}, language={}", messageKey, language);

        // Try cache first
        Map<String, String> cachedMessages = getCachedMessages(language);
        if (cachedMessages != null && cachedMessages.containsKey(messageKey)) {
            log.debug("✅ Cache HIT for key={}, language={}", messageKey, language);
            return cachedMessages.get(messageKey);
        }

        // Cache miss - fallback to database
        log.debug("⚠️ Cache MISS for key={}, language={}", messageKey, language);
        return getMessageWithFallback(messageKey, language);
    }

    /**
     * Get all messages for a language (bulk operation)
     * <p>Optimized for frontend bulk loading</p>
     *
     * <p><strong>Use Case:</strong></p>
     * Frontend calls this once at startup to load all translations,
     * avoiding individual API calls for each message.
     *
     * <p><strong>Response Format:</strong></p>
     * <pre>
     * {
     *   "button.save": "Сохранить",
     *   "button.cancel": "Отмена",
     *   "error.not_found": "Не найдено"
     * }
     * </pre>
     *
     * @param language Language code (e.g., "ru-RU")
     * @return Map of messageKey → translation
     */
    public Map<String, String> getAllMessages(String language) {
        log.info("Getting all messages for language: {}", language);

        // Try cache first
        Map<String, String> cachedMessages = getCachedMessages(language);
        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            log.info("✅ Cache HIT for all messages, language={}, count={}", language, cachedMessages.size());
            return cachedMessages;
        }

        // Cache miss - load from database and cache
        log.info("⚠️ Cache MISS for all messages, language={}", language);
        Map<String, String> messages = loadFromDatabaseBulk(language);
        cacheMessages(language, messages);

        return messages;
    }

    /**
     * Get messages by category (e.g., all button labels)
     * <p>Filtered from bulk cache data</p>
     *
     * @param category Message category (app, menu, button, etc.)
     * @param language Language code
     * @return Map of messageKey → translation for this category
     */
    public Map<String, String> getMessagesByCategory(String category, String language) {
        log.debug("Getting messages for category={}, language={}", category, language);

        Map<String, String> allMessages = getAllMessages(language);

        // Filter by category prefix
        String categoryPrefix = category + ".";
        return allMessages.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(categoryPrefix))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Invalidate cache for specific language
     * <p>Called after translation updates in admin panel</p>
     *
     * <p><strong>Use Case:</strong></p>
     * Admin updates Russian translations → invalidate "ru-RU" cache →
     * next request will reload fresh data from database
     *
     * @param language Language code to invalidate
     */
    public void invalidateCache(String language) {
        log.info("Invalidating cache for language: {}", language);

        String cacheKey = CACHE_KEY_PREFIX + language;
        Boolean deleted = redisTemplate.delete(cacheKey);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("✅ Cache invalidated successfully for language: {}", language);
        } else {
            log.warn("⚠️ Cache key not found or deletion failed for language: {}", language);
        }
    }

    /**
     * Invalidate all language caches
     * <p>Called after bulk translation updates</p>
     */
    public void invalidateAllCaches() {
        log.info("Invalidating all I18n caches");

        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            Long deleted = redisTemplate.delete(keys);
            log.info("✅ Invalidated {} cache keys", deleted);
        } else {
            log.info("ℹ️ No cache keys found to invalidate");
        }
    }

    /**
     * Clear all caches (alias for invalidateAllCaches)
     * <p>User-friendly name for admin API</p>
     */
    public void clearCache() {
        invalidateAllCaches();
    }

    /**
     * Get cache statistics
     * <p>For monitoring and debugging</p>
     *
     * @return Map with cache stats
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        stats.put("cachedLanguages", keys != null ? keys.size() : 0);
        stats.put("cacheKeyPrefix", CACHE_KEY_PREFIX);
        stats.put("cacheTTL", CACHE_TTL.toString());

        if (keys != null) {
            List<String> languageList = keys.stream()
                .map(key -> key.replace(CACHE_KEY_PREFIX, ""))
                .collect(Collectors.toList());
            stats.put("languages", languageList);
        }

        return stats;
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * Get cached messages for language from Redis
     *
     * @param language Language code
     * @return Map of messages or null if not cached
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> getCachedMessages(String language) {
        String cacheKey = CACHE_KEY_PREFIX + language;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Map) {
                return (Map<String, String>) cached;
            }
        } catch (Exception e) {
            log.error("Redis error when getting cache for language: {}", language, e);
        }

        return null;
    }

    /**
     * Cache messages for language in Redis
     *
     * @param language Language code
     * @param messages Map of messageKey → translation
     */
    private void cacheMessages(String language, Map<String, String> messages) {
        String cacheKey = CACHE_KEY_PREFIX + language;

        try {
            redisTemplate.opsForValue().set(cacheKey, messages, CACHE_TTL);
            log.debug("✅ Cached {} messages for language: {}", messages.size(), language);
        } catch (Exception e) {
            log.error("Redis error when caching messages for language: {}", language, e);
        }
    }

    /**
     * Load all translations for language from database (bulk)
     * <p>Single query with JOIN FETCH optimization</p>
     *
     * <p><strong>IMPORTANT - uz-UZ Handling:</strong></p>
     * <ul>
     *   <li>uz-UZ uses default message column (no translation table entries)</li>
     *   <li>Other languages use translation table</li>
     * </ul>
     *
     * @param language Language code
     * @return Map of messageKey → translation
     */
    @Transactional(readOnly = true)
    protected Map<String, String> loadFromDatabaseBulk(String language) {
        log.debug("Loading translations from database for language: {}", language);

        // SPECIAL CASE: uz-UZ uses default message column
        if (DEFAULT_LANGUAGE.equals(language)) {
            List<SystemMessage> messages = systemMessageRepository.findByIsActiveTrue();

            Map<String, String> result = messages.stream()
                .collect(Collectors.toMap(
                    SystemMessage::getMessageKey,
                    SystemMessage::getMessage,
                    (existing, replacement) -> existing  // Keep first if duplicate
                ));

            log.debug("Loaded {} default messages (uz-UZ) from database", result.size());
            return result;
        }

        // NORMAL CASE: Load from translation table
        List<SystemMessageTranslation> translations =
            translationRepository.findByLanguageWithMessage(language);

        Map<String, String> result = translations.stream()
            .collect(Collectors.toMap(
                t -> t.getSystemMessage().getMessageKey(),
                SystemMessageTranslation::getTranslation,
                (existing, replacement) -> existing  // Keep first if duplicate
            ));

        log.debug("Loaded {} translations from database for language: {}", result.size(), language);
        return result;
    }

    /**
     * Get message with UNIVER fallback logic
     * <p>Exact match → language prefix → default (uz)</p>
     *
     * @param messageKey Message key
     * @param language Language code
     * @return Translation (never null)
     */
    @Transactional(readOnly = true)
    protected String getMessageWithFallback(String messageKey, String language) {
        // 1st Fallback: Database
        Optional<SystemMessage> messageOpt = systemMessageRepository
            .findByMessageKeyWithTranslations(messageKey);

        if (messageOpt.isPresent()) {
            SystemMessage message = messageOpt.get();

            // Try exact language match (e.g., ru-RU)
            String translation = message.getTranslation(language);
            if (translation != null) {
                return translation;
            }

            // Try language without region (e.g., ru)
            if (language.contains("-")) {
                String languageOnly = language.split("-")[0];
                translation = message.getTranslations().stream()
                    .filter(t -> t.languageStartsWith(languageOnly))
                    .findFirst()
                    .map(SystemMessageTranslation::getTranslation)
                    .orElse(null);

                if (translation != null) {
                    return translation;
                }
            }

            // Fallback to default message (Uzbek)
            return message.getMessage();
        }

        // 2nd Fallback: Properties File
        String propertiesTranslation = getFromPropertiesFile(messageKey, language);
        if (propertiesTranslation != null) {
            log.debug("✅ Found in properties file: key={}, language={}", messageKey, language);
            return propertiesTranslation;
        }

        // 3rd Fallback: Return key itself
        log.warn("⚠️  Message key not found anywhere: {}", messageKey);
        return messageKey;
    }

    /**
     * Get translation from properties file cache
     *
     * @param messageKey Message key
     * @param language Language code
     * @return Translation or null if not found
     */
    private String getFromPropertiesFile(String messageKey, String language) {
        Properties props = propertiesCache.get(language);
        if (props != null && props.containsKey(messageKey)) {
            return props.getProperty(messageKey);
        }

        // Try language without region
        if (language.contains("-")) {
            String languageOnly = language.split("-")[0];
            for (Map.Entry<String, Properties> entry : propertiesCache.entrySet()) {
                if (entry.getKey().startsWith(languageOnly)) {
                    Properties langProps = entry.getValue();
                    if (langProps.containsKey(messageKey)) {
                        return langProps.getProperty(messageKey);
                    }
                }
            }
        }

        return null;
    }
}
