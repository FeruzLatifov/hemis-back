package uz.hemis.service.translation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.hemis.domain.repository.TranslationRepository;

import java.util.List;
import java.util.Map;

/**
 * Translation Service
 * Provides translation functionality for multi-language support
 *
 * @author System Architect
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;

    /**
     * Get translation for a specific key and locale
     * Returns the key itself if translation not found
     */
    public String translate(String key, String locale) {
        if (key == null || key.isBlank()) {
            return key;
        }

        return translationRepository.findByKeyAndLocale(key, locale)
            .map(t -> {
                log.trace("Translation found: {} -> {} ({})", key, t.getValue(), locale);
                return t.getValue();
            })
            .orElseGet(() -> {
                log.warn("Translation not found for key: {} (locale: {})", key, locale);
                return key; // Fallback to key itself
            });
    }

    /**
     * Get translations for multiple keys
     */
    public Map<String, String> translateBatch(List<String> keys, String locale) {
        return translationRepository.findByKeysAndLocale(keys, locale).stream()
            .collect(java.util.stream.Collectors.toMap(
                t -> t.getKey(),
                t -> t.getValue(),
                (v1, v2) -> v1 // Keep first value in case of duplicates
            ));
    }

    /**
     * Get all translations for a locale as a Map
     * Cached for performance
     */
    @Cacheable(value = "translations", key = "#locale")
    public Map<String, String> getAllTranslations(String locale) {
        log.debug("Loading all translations for locale: {}", locale);
        return translationRepository.getTranslationsMap(locale);
    }

    /**
     * Get translations by category
     * Useful for menu, forms, etc.
     */
    @Cacheable(value = "translations-category", key = "#locale + '-' + #category")
    public Map<String, String> getTranslationsByCategory(String locale, String category) {
        log.debug("Loading translations for locale: {}, category: {}", locale, category);
        return translationRepository.getTranslationsMapByCategory(locale, category);
    }

    /**
     * Normalize locale string
     * Examples: "uz-UZ", "ru-RU", "en-US"
     */
    public String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "uz-UZ"; // Default locale
        }

        // Convert to standard format
        locale = locale.trim();

        // Handle common variations
        if (locale.equalsIgnoreCase("uz") || locale.equalsIgnoreCase("uzb")) {
            return "uz-UZ";
        }
        if (locale.equalsIgnoreCase("ru") || locale.equalsIgnoreCase("rus")) {
            return "ru-RU";
        }
        if (locale.equalsIgnoreCase("en") || locale.equalsIgnoreCase("eng")) {
            return "en-US";
        }

        return locale;
    }
}
