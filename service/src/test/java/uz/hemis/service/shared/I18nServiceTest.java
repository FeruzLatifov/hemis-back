package uz.hemis.service.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import uz.hemis.domain.entity.SystemMessage;
import uz.hemis.domain.repository.SystemMessageRepository;
import uz.hemis.domain.repository.SystemMessageTranslationRepository;
import uz.hemis.service.cache.CacheVersionService;
import uz.hemis.service.config.LanguageProperties;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link I18nService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getMessage - cache hit, fallback logic</li>
 *   <li>getAllMessages - bulk loading from database</li>
 *   <li>getMessagesByCategory - category-based filtering</li>
 *   <li>getCategoryKeys - cache miss loads from DB</li>
 *   <li>invalidateCache - evicts specific language cache</li>
 *   <li>invalidateAllCaches - clears all caches</li>
 *   <li>getCacheStats - returns stats map</li>
 * </ul>
 *
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("I18nService Unit Tests")
class I18nServiceTest {

    @Mock
    private SystemMessageRepository systemMessageRepository;

    @Mock
    private SystemMessageTranslationRepository translationRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private CacheVersionService cacheVersionService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private LanguageProperties languageProperties;

    @InjectMocks
    private I18nService i18nService;

    // =====================================================
    // getMessage tests
    // =====================================================

    @Nested
    @DisplayName("getMessage")
    class GetMessage {

        @Test
        @DisplayName("returns translated message when found in bulk cache")
        void returnsTranslatedMessageFromCache() {
            // Given
            String messageKey = "Save";
            String language = "uz-UZ";

            // Spy the service to control getAllMessages behavior
            // Since getAllMessages is @Cacheable and we are unit testing,
            // we simulate the database loading path.
            when(languageProperties.getDefaultLocale()).thenReturn("uz-UZ");

            SystemMessage msg = SystemMessage.builder()
                    .messageKey("Save")
                    .message("Saqlash")
                    .category("action")
                    .isActive(true)
                    .build();
            when(systemMessageRepository.findByIsActiveTrue()).thenReturn(List.of(msg));

            // When
            String result = i18nService.getMessage(messageKey, language);

            // Then
            assertThat(result).isEqualTo("Saqlash");
        }

        @Test
        @DisplayName("returns message key itself when no translation found anywhere")
        void returnsMessageKeyWhenNotFound() {
            // Given
            String messageKey = "NonExistentKey";
            String language = "uz-UZ";

            when(languageProperties.getDefaultLocale()).thenReturn("uz-UZ");
            when(systemMessageRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());
            when(systemMessageRepository.findByMessageKeyWithTranslations("NonExistentKey"))
                    .thenReturn(Optional.empty());

            // When
            String result = i18nService.getMessage(messageKey, language);

            // Then
            assertThat(result).isEqualTo("NonExistentKey");
        }
    }

    // =====================================================
    // getAllMessages tests
    // =====================================================

    @Nested
    @DisplayName("getAllMessages")
    class GetAllMessages {

        @Test
        @DisplayName("loads default language messages from system_messages table")
        void loadsDefaultLanguageMessages() {
            // Given
            String language = "uz-UZ";
            when(languageProperties.getDefaultLocale()).thenReturn("uz-UZ");

            SystemMessage msg1 = SystemMessage.builder()
                    .messageKey("Save")
                    .message("Saqlash")
                    .category("action")
                    .isActive(true)
                    .build();
            SystemMessage msg2 = SystemMessage.builder()
                    .messageKey("Cancel")
                    .message("Bekor qilish")
                    .category("action")
                    .isActive(true)
                    .build();

            when(systemMessageRepository.findByIsActiveTrue()).thenReturn(List.of(msg1, msg2));

            // When
            Map<String, String> messages = i18nService.getAllMessages(language);

            // Then
            assertThat(messages).hasSize(2);
            assertThat(messages).containsEntry("Save", "Saqlash");
            assertThat(messages).containsEntry("Cancel", "Bekor qilish");
        }

        @Test
        @DisplayName("loads non-default language messages from translation table")
        void loadsNonDefaultLanguageMessages() {
            // Given
            String language = "ru-RU";
            when(languageProperties.getDefaultLocale()).thenReturn("uz-UZ");
            when(translationRepository.findByLanguageWithMessage(language)).thenReturn(Collections.emptyList());

            // When
            Map<String, String> messages = i18nService.getAllMessages(language);

            // Then
            assertThat(messages).isEmpty();
            verify(translationRepository).findByLanguageWithMessage(language);
        }
    }

    // =====================================================
    // getMessagesByCategory tests
    // =====================================================

    @Nested
    @DisplayName("getMessagesByCategory")
    class GetMessagesByCategory {

        @Test
        @DisplayName("filters all messages by category keys")
        void filtersMessagesByCategory() {
            // Given
            String category = "action";
            String language = "uz-UZ";

            SystemMessage actionMsg = SystemMessage.builder()
                    .messageKey("Save")
                    .message("Saqlash")
                    .category("action")
                    .isActive(true)
                    .build();
            when(systemMessageRepository.findByCategoryAndIsActiveTrue(category))
                    .thenReturn(List.of(actionMsg));
            when(languageProperties.getDefaultLocale()).thenReturn("uz-UZ");

            SystemMessage otherMsg = SystemMessage.builder()
                    .messageKey("Dashboard")
                    .message("Bosh sahifa")
                    .category("menu")
                    .isActive(true)
                    .build();
            when(systemMessageRepository.findByIsActiveTrue())
                    .thenReturn(List.of(actionMsg, otherMsg));

            // When
            Map<String, String> result = i18nService.getMessagesByCategory(category, language);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).containsEntry("Save", "Saqlash");
            assertThat(result).doesNotContainKey("Dashboard");
        }
    }

    // =====================================================
    // getCategoryKeys tests
    // =====================================================

    @Nested
    @DisplayName("getCategoryKeys")
    class GetCategoryKeys {

        @Test
        @DisplayName("returns set of message keys for given category from database")
        void returnsCategoryKeysFromDatabase() {
            // Given
            String category = "action";
            SystemMessage msg1 = SystemMessage.builder()
                    .messageKey("Save")
                    .message("Saqlash")
                    .category("action")
                    .isActive(true)
                    .build();
            SystemMessage msg2 = SystemMessage.builder()
                    .messageKey("Delete")
                    .message("O'chirish")
                    .category("action")
                    .isActive(true)
                    .build();

            when(systemMessageRepository.findByCategoryAndIsActiveTrue(category))
                    .thenReturn(List.of(msg1, msg2));

            // When
            Set<String> keys = i18nService.getCategoryKeys(category);

            // Then
            assertThat(keys).containsExactlyInAnyOrder("Save", "Delete");
        }

        @Test
        @DisplayName("returns empty set when no messages in category")
        void returnsEmptySetForUnknownCategory() {
            // Given
            when(systemMessageRepository.findByCategoryAndIsActiveTrue("nonexistent"))
                    .thenReturn(Collections.emptyList());

            // When
            Set<String> keys = i18nService.getCategoryKeys("nonexistent");

            // Then
            assertThat(keys).isEmpty();
        }
    }

    // =====================================================
    // invalidateCache tests
    // =====================================================

    @Nested
    @DisplayName("invalidateCache")
    class InvalidateCache {

        @Test
        @DisplayName("evicts cache for specific language and publishes invalidation event")
        void evictsCacheForLanguage() {
            // Given
            String language = "ru-RU";
            Cache mockCache = mock(Cache.class);
            when(cacheManager.getCache("i18n")).thenReturn(mockCache);
            when(cacheVersionService.incrementVersionAndPublish("i18n")).thenReturn(2L);

            // When
            i18nService.invalidateCache(language);

            // Then
            verify(mockCache).evict("messages:ru-RU");
            verify(cacheVersionService).incrementVersionAndPublish("i18n");
        }
    }

    // =====================================================
    // invalidateAllCaches tests
    // =====================================================

    @Nested
    @DisplayName("invalidateAllCaches")
    class InvalidateAllCaches {

        @Test
        @DisplayName("clears both i18n and i18n-scope caches and publishes event")
        void clearsBothCachesAndPublishes() {
            // Given
            Cache i18nCache = mock(Cache.class);
            Cache scopeCache = mock(Cache.class);
            when(cacheManager.getCache("i18n")).thenReturn(i18nCache);
            when(cacheManager.getCache("i18n-scope")).thenReturn(scopeCache);
            when(cacheVersionService.incrementVersionAndPublish("i18n")).thenReturn(3L);

            // When
            i18nService.invalidateAllCaches();

            // Then
            verify(i18nCache).clear();
            verify(scopeCache).clear();
            verify(cacheVersionService).incrementVersionAndPublish("i18n");
        }
    }

    // =====================================================
    // getCacheStats tests
    // =====================================================

    @Nested
    @DisplayName("getCacheStats")
    class GetCacheStats {

        @Test
        @DisplayName("returns cache stats map with expected keys")
        void returnsCacheStatsMap() {
            // Given
            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ", "ru-RU", "en-US"));
            when(cacheVersionService.getCurrentVersion("i18n")).thenReturn(5L);

            // When
            Map<String, Object> stats = i18nService.getCacheStats();

            // Then
            assertThat(stats).containsEntry("cacheName", "i18n");
            assertThat(stats).containsEntry("cacheType", "TwoLevelCache (L1 Caffeine + L2 Redis)");
            assertThat(stats).containsEntry("currentVersion", 5L);
            assertThat(stats).containsKey("languages");
            assertThat((List<?>) stats.get("languages")).hasSize(3);
        }
    }
}
