package uz.hemis.service.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uz.hemis.domain.entity.Language;
import uz.hemis.domain.repository.LanguageRepository;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Language Configuration Properties - DATABASE-DRIVEN ✅
 *
 * <p><strong>Architecture Change (v2.1):</strong></p>
 * <pre>
 * ❌ OLD: Static YAML configuration (requires restart)
 * ✅ NEW: Database-driven with YAML fallback (dynamic, no restart!)
 * </pre>
 *
 * <p><strong>Data Source Priority:</strong></p>
 * <ol>
 *   <li><strong>PRIMARY:</strong> Database (languages table, is_active=true)</li>
 *   <li><strong>FALLBACK:</strong> YAML configuration (app.languages.supported)</li>
 * </ol>
 *
 * <p><strong>Benefits:</strong></p>
 * <ul>
 *   <li>✅ Admin panel can enable/disable languages (no restart)</li>
 *   <li>✅ Dynamic language management via UI</li>
 *   <li>✅ Database as single source of truth</li>
 *   <li>✅ YAML fallback for bootstrap/testing</li>
 *   <li>✅ Cached for performance (refresh on language CRUD)</li>
 * </ul>
 *
 * <p><strong>Adding New Languages:</strong></p>
 * <ol>
 *   <li>Insert into languages table: <code>INSERT INTO languages (code, name, native_name, is_active) VALUES ('kk-UZ', 'Karakalpak', 'Қарақалпақша', true)</code></li>
 *   <li>Add translations to system_message_translations table</li>
 *   <li>✅ DONE! No restart, no code changes!</li>
 * </ol>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * &#64;Service
 * &#64;RequiredArgsConstructor
 * public class MenuService {
 *     private final LanguageProperties languageProperties;
 *
 *     public void warmupCache() {
 *         // ✅ Reads from database (cached)
 *         for (String locale : languageProperties.getSupported()) {
 *             cacheMenu(locale);
 *         }
 *     }
 * }
 * </pre>
 *
 * @since 2.1.0 (database-driven)
 */
@Configuration
@ConfigurationProperties(prefix = "app.languages")
@Data
@Slf4j
public class LanguageProperties {

    private final LanguageRepository languageRepository;

    /**
     * YAML fallback configuration (used during bootstrap)
     *
     * <p><strong>IMPORTANT:</strong> This is a FALLBACK only!</p>
     * <p>Primary source: Database (languages table, is_active=true)</p>
     *
     * <p><strong>Default:</strong> uz-UZ, oz-UZ, ru-RU, en-US</p>
     *
     * <p><strong>Format:</strong> language-REGION (ISO 639-1 + region code)</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>uz-UZ - Uzbek (Latin script)</li>
     *   <li>oz-UZ - Uzbek (Cyrillic script)</li>
     *   <li>ru-RU - Russian</li>
     *   <li>en-US - English (US)</li>
     *   <li>kk-UZ - Karakalpak (future)</li>
     *   <li>tg-TG - Tajik (future)</li>
     * </ul>
     */
    private List<String> supported = new ArrayList<>(Arrays.asList(
        "uz-UZ",  // Uzbek Latin (primary)
        "oz-UZ",  // Uzbek Cyrillic
        "ru-RU",  // Russian
        "en-US"   // English
    ));

    /**
     * Cached list of active languages from database
     *
     * <p><strong>Cache Strategy:</strong></p>
     * <ul>
     *   <li>Loaded on application startup (@PostConstruct)</li>
     *   <li>Refreshed when admin updates languages table</li>
     *   <li>Falls back to YAML if database is unavailable</li>
     * </ul>
     *
     * <p><strong>Thread Safety:</strong> volatile ensures visibility across threads</p>
     */
    private volatile List<String> cachedActiveLanguages = null;

    /**
     * Default language code
     *
     * <p><strong>Default:</strong> uz-UZ (Uzbek Latin)</p>
     *
     * <p>Used when:</p>
     * <ul>
     *   <li>User has no locale preference set</li>
     *   <li>Requested locale is not supported</li>
     *   <li>Translation key is missing in user's locale (fallback)</li>
     * </ul>
     */
    private String defaultLocale = "uz-UZ";

    /**
     * Whether to enable automatic locale detection from browser
     *
     * <p><strong>Default:</strong> true</p>
     *
     * <p>If enabled, system detects locale from:</p>
     * <ol>
     *   <li>User profile setting (highest priority)</li>
     *   <li>Accept-Language HTTP header (browser preference)</li>
     *   <li>Default locale (fallback)</li>
     * </ol>
     */
    private boolean autoDetect = true;

    // =====================================================
    // Database Loading & Cache Management
    // =====================================================

    /**
     * Load active languages from database on application startup
     *
     * <p><strong>Execution Order:</strong></p>
     * <ol>
     *   <li>Spring creates bean and injects LanguageRepository</li>
     *   <li>@PostConstruct triggers this method</li>
     *   <li>Query database for active languages (ORDER BY position)</li>
     *   <li>Cache results in cachedActiveLanguages</li>
     *   <li>Fall back to YAML if database unavailable</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong></p>
     * <p>If database query fails (e.g., during integration tests, first boot),
     * falls back to YAML configuration. This ensures system can start even
     * if database is not ready.</p>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Runs once on startup (~10ms)</li>
     *   <li>Cached for entire application lifetime</li>
     *   <li>Can be refreshed via refreshFromDatabase()</li>
     * </ul>
     */
    @PostConstruct
    public void loadActiveLanguagesFromDatabase() {
        try {
            List<Language> activeLanguages = languageRepository.findAllActiveOrderedByPosition();

            if (activeLanguages.isEmpty()) {
                log.warn("⚠️  No active languages found in database, using YAML fallback: {}", supported);
                cachedActiveLanguages = new ArrayList<>(supported);
                return;
            }

            cachedActiveLanguages = activeLanguages.stream()
                .map(Language::getCode)
                .collect(Collectors.toList());

            log.info("✅ Loaded {} active languages from database: {}",
                cachedActiveLanguages.size(), cachedActiveLanguages);

            // Validate fallback locale is in active list
            if (!cachedActiveLanguages.contains(defaultLocale)) {
                log.warn("⚠️  Default locale '{}' not in active languages! Using first active: {}",
                    defaultLocale, cachedActiveLanguages.get(0));
                defaultLocale = cachedActiveLanguages.get(0);
            }

        } catch (Exception e) {
            log.error("❌ Failed to load languages from database, using YAML fallback: {}", supported, e);
            cachedActiveLanguages = new ArrayList<>(supported);
        }
    }

    /**
     * Refresh active languages from database
     *
     * <p><strong>Use Case:</strong></p>
     * <p>Called when admin enables/disables languages via admin panel.
     * Allows dynamic language management without application restart.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * // In LanguageAdminService after updating language.is_active
     * languageRepository.save(language);
     * languageProperties.refreshFromDatabase();
     * i18nService.invalidateAllCaches(); // Clear translation caches
     * menuService.invalidateMenuCache(); // Clear menu caches
     * </pre>
     *
     * <p><strong>Cross-Pod Sync:</strong></p>
     * <p>In multi-pod deployments, publish Redis Pub/Sub event to notify
     * other pods to refresh their caches.</p>
     */
    public void refreshFromDatabase() {
        log.info("🔄 Refreshing active languages from database...");
        loadActiveLanguagesFromDatabase();
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Get list of supported languages (DATABASE-DRIVEN)
     *
     * <p><strong>Data Source Priority:</strong></p>
     * <ol>
     *   <li>Database cache (loaded via @PostConstruct)</li>
     *   <li>YAML fallback (if database unavailable)</li>
     * </ol>
     *
     * <p><strong>Usage:</strong></p>
     * <pre>
     * for (String locale : languageProperties.getSupported()) {
     *     // Process each active language from database
     * }
     * </pre>
     *
     * @return List of active language codes from database or YAML fallback
     */
    public List<String> getSupported() {
        return cachedActiveLanguages != null ? cachedActiveLanguages : supported;
    }

    /**
     * Check if locale is supported (DATABASE-DRIVEN)
     *
     * @param locale Language code (e.g., "uz-UZ")
     * @return true if locale is in active database list
     */
    public boolean isSupported(String locale) {
        return getSupported().contains(locale);
    }

    /**
     * Get locale or fallback to default
     *
     * @param locale Requested locale
     * @return Locale if supported, otherwise default locale
     */
    public String getOrDefault(String locale) {
        return isSupported(locale) ? locale : defaultLocale;
    }

    /**
     * Get supported locales as array (DATABASE-DRIVEN)
     *
     * <p><strong>Performance:</strong> Creates new array on each call,
     * consider caching result if called frequently</p>
     *
     * @return Array of active language codes from database
     */
    public String[] getSupportedArray() {
        return getSupported().toArray(new String[0]);
    }
}
