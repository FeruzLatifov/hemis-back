package uz.hemis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import uz.hemis.service.cache.CacheVersionService;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for I18nService versioned cache behavior.
 *
 * Tests:
 * - Cache key versioning
 * - Cache invalidation via version increment
 * - Multi-language cache management
 * - TTL verification
 *
 * @author Senior Architect
 * @since 2025-11-13
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("I18n Service Versioned Cache Tests")
class I18nServiceCacheTest {

    @Autowired
    private I18nService i18nService;

    @Autowired
    private CacheVersionService cacheVersionService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String I18N_NAMESPACE = "i18n";

    @BeforeEach
    void setUp() {
        // Clean up i18n cache before each test
        Set<String> keys = redisTemplate.keys("i18n:v*:messages:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        // Reset version
        redisTemplate.delete("cache:version:" + I18N_NAMESPACE);
    }

    @Test
    @DisplayName("Should use versioned cache keys for translations")
    void testVersionedCacheKeys() {
        // Given
        String language = "uz-UZ";
        long initialVersion = cacheVersionService.getCurrentVersion(I18N_NAMESPACE);

        // When - First call loads from DB and caches
        Map<String, String> translations1 = i18nService.getAllMessages(language);

        // Then - Check versioned key exists in Redis
        String expectedKey = String.format("i18n:v%d:messages:%s", initialVersion, language);
        Boolean exists = redisTemplate.hasKey(expectedKey);
        assertThat(exists).isTrue();

        // When - Invalidate cache (increments version)
        i18nService.invalidateCache(language);
        long newVersion = cacheVersionService.getCurrentVersion(I18N_NAMESPACE);

        // Then - Version should increment
        assertThat(newVersion).isEqualTo(initialVersion + 1);

        // When - Next call uses new version
        Map<String, String> translations2 = i18nService.getAllMessages(language);

        // Then - New versioned key should exist
        String newKey = String.format("i18n:v%d:messages:%s", newVersion, language);
        Boolean newKeyExists = redisTemplate.hasKey(newKey);
        assertThat(newKeyExists).isTrue();

        // Old key should still exist (for zero-downtime)
        Boolean oldKeyExists = redisTemplate.hasKey(expectedKey);
        assertThat(oldKeyExists).isTrue();
    }

    @Test
    @DisplayName("Should handle multiple language caches independently")
    void testMultiLanguageCache() {
        // Given
        String[] languages = {"uz-UZ", "oz-UZ", "ru-RU", "en-US"};

        // When - Load translations for all languages
        for (String lang : languages) {
            i18nService.getAllMessages(lang);
        }

        // Then - All versioned keys should exist
        long version = cacheVersionService.getCurrentVersion(I18N_NAMESPACE);
        for (String lang : languages) {
            String expectedKey = String.format("i18n:v%d:messages:%s", version, lang);
            Boolean exists = redisTemplate.hasKey(expectedKey);
            assertThat(exists)
                .withFailMessage("Cache key should exist for language: " + lang)
                .isTrue();
        }
    }

    @Test
    @DisplayName("Should invalidate all language caches via version increment")
    void testInvalidateAllCaches() {
        // Given
        String[] languages = {"uz-UZ", "ru-RU"};
        for (String lang : languages) {
            i18nService.getAllMessages(lang);
        }

        long versionBefore = cacheVersionService.getCurrentVersion(I18N_NAMESPACE);

        // When - Invalidate all caches
        i18nService.invalidateAllCaches();

        // Then - Version should increment
        long versionAfter = cacheVersionService.getCurrentVersion(I18N_NAMESPACE);
        assertThat(versionAfter).isEqualTo(versionBefore + 1);

        // When - Load translations again
        for (String lang : languages) {
            i18nService.getAllMessages(lang);
        }

        // Then - New versioned keys should be created
        for (String lang : languages) {
            String newKey = String.format("i18n:v%d:messages:%s", versionAfter, lang);
            Boolean exists = redisTemplate.hasKey(newKey);
            assertThat(exists)
                .withFailMessage("New cache key should exist after invalidation: " + lang)
                .isTrue();
        }
    }

    @Test
    @DisplayName("Should include cache statistics with version info")
    void testCacheStats() {
        // Given
        i18nService.getAllMessages("uz-UZ");

        // When
        Map<String, Object> stats = i18nService.getCacheStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys(
            "currentVersion",
            "cacheKeyPattern",
            "cacheTTL"
        );

        Long currentVersion = (Long) stats.get("currentVersion");
        assertThat(currentVersion).isGreaterThanOrEqualTo(1L);

        String cacheKeyPattern = (String) stats.get("cacheKeyPattern");
        assertThat(cacheKeyPattern).contains("i18n:v" + currentVersion + ":messages:");
    }

    @Test
    @DisplayName("Should handle cache TTL of 30 minutes")
    void testCacheTTL() {
        // Given
        String language = "uz-UZ";
        long version = cacheVersionService.getCurrentVersion(I18N_NAMESPACE);

        // When
        i18nService.getAllMessages(language);

        // Then - Check TTL is set correctly
        String cacheKey = String.format("i18n:v%d:messages:%s", version, language);
        Long ttl = redisTemplate.getExpire(cacheKey);

        // TTL should be > 0 and <= 1800 seconds (30 minutes)
        assertThat(ttl).isGreaterThan(0L);
        assertThat(ttl).isLessThanOrEqualTo(1800L);
    }

    @Test
    @DisplayName("Should return translations from cache on second call (cache hit)")
    void testCacheHit() {
        // Given
        String language = "uz-UZ";

        // When - First call (cache miss, loads from DB)
        long startTime1 = System.currentTimeMillis();
        Map<String, String> translations1 = i18nService.getAllMessages(language);
        long duration1 = System.currentTimeMillis() - startTime1;

        // When - Second call (cache hit, loads from Redis)
        long startTime2 = System.currentTimeMillis();
        Map<String, String> translations2 = i18nService.getAllMessages(language);
        long duration2 = System.currentTimeMillis() - startTime2;

        // Then
        assertThat(translations1).isNotNull();
        assertThat(translations2).isNotNull();
        assertThat(translations1).isEqualTo(translations2);

        // Cache hit should be significantly faster
        // (This is approximate - may vary based on system load)
        assertThat(duration2).isLessThan(duration1);
    }

    @Test
    @DisplayName("Should survive version increment without data loss")
    void testZeroDowntimeInvalidation() {
        // Given
        String language = "uz-UZ";
        long v1 = cacheVersionService.getCurrentVersion(I18N_NAMESPACE);

        // When - Load translations at version 1
        Map<String, String> translations1 = i18nService.getAllMessages(language);
        assertThat(translations1).isNotEmpty();

        String keyV1 = String.format("i18n:v%d:messages:%s", v1, language);
        assertThat(redisTemplate.hasKey(keyV1)).isTrue();

        // When - Increment version (simulate cache invalidation)
        long v2 = cacheVersionService.incrementVersion(I18N_NAMESPACE);

        // Then - Old key still exists (zero downtime)
        assertThat(redisTemplate.hasKey(keyV1)).isTrue();

        // When - Load translations at version 2
        Map<String, String> translations2 = i18nService.getAllMessages(language);

        // Then - New key exists, data is preserved
        String keyV2 = String.format("i18n:v%d:messages:%s", v2, language);
        assertThat(redisTemplate.hasKey(keyV2)).isTrue();
        assertThat(translations2).isNotEmpty();
        assertThat(translations2).isEqualTo(translations1);
    }
}
