package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.SystemMessageTranslation;
import uz.hemis.domain.entity.SystemMessageTranslationId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SystemMessageTranslation Entity
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Data access layer for message translations</li>
 *   <li>Composite PK support (messageId, language)</li>
 *   <li>Language-specific queries</li>
 * </ul>
 *
 * <p><strong>UNIVER Pattern:</strong></p>
 * <ul>
 *   <li>Similar to SystemMessageTranslationRepository in UNIVER</li>
 *   <li>Supports finding by message ID and language</li>
 *   <li>Bulk loading by language for caching</li>
 * </ul>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Find specific translation
 * SystemMessageTranslationId id = new SystemMessageTranslationId(messageId, "ru-RU");
 * Optional&lt;SystemMessageTranslation&gt; translation = repository.findById(id);
 *
 * // Find all translations for a message
 * List&lt;SystemMessageTranslation&gt; translations = repository.findByMessageId(messageId);
 *
 * // Find all Russian translations
 * List&lt;SystemMessageTranslation&gt; russianTranslations = repository.findByLanguage("ru-RU");
 * </pre>
 *
 * @see SystemMessageTranslation
 * @see SystemMessageTranslationId
 * @since 2.0.0
 */
@Repository
public interface SystemMessageTranslationRepository extends
        JpaRepository<SystemMessageTranslation, SystemMessageTranslationId> {

    /**
     * Find all translations for a specific message
     * <p>Returns all language versions of one message</p>
     *
     * @param messageId SystemMessage ID
     * @return List of translations for this message
     */
    List<SystemMessageTranslation> findByMessageId(UUID messageId);

    /**
     * Find all translations for a specific language
     * <p>Used for bulk loading all translations for one language</p>
     *
     * <p><strong>Use case:</strong></p>
     * Load all Russian translations at once for Redis cache
     *
     * @param language Language code (e.g., "ru-RU")
     * @return List of all translations in this language
     */
    List<SystemMessageTranslation> findByLanguage(String language);

    /**
     * Find translation for specific message and language
     * <p>Alternative to findById with separate parameters</p>
     *
     * @param messageId SystemMessage ID
     * @param language Language code
     * @return Optional of translation
     */
    Optional<SystemMessageTranslation> findByMessageIdAndLanguage(UUID messageId, String language);

    /**
     * Find all translations for a language with message details
     * <p>Eagerly loads SystemMessage to avoid N+1 queries</p>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Single query with JOIN FETCH</li>
     *   <li>Message key included in result</li>
     *   <li>Ideal for API response generation</li>
     * </ul>
     *
     * @param language Language code
     * @return List of translations with message details
     */
    @Query("SELECT t FROM SystemMessageTranslation t " +
           "JOIN FETCH t.systemMessage m " +
           "WHERE t.language = :language " +
           "AND m.deletedAt IS NULL " +
           "AND m.isActive = true")
    List<SystemMessageTranslation> findByLanguageWithMessage(@Param("language") String language);

    /**
     * Find translations by language prefix
     * <p>For fallback logic: find "ru" translations when "ru-RU" not found</p>
     *
     * <p><strong>Fallback pattern:</strong></p>
     * <ul>
     *   <li>Request: ru-RU → not found</li>
     *   <li>Fallback: search language starts with "ru"</li>
     *   <li>May return: ru-RU, ru-KZ, etc.</li>
     * </ul>
     *
     * @param languagePrefix Language prefix (e.g., "ru")
     * @return List of translations matching prefix
     */
    @Query("SELECT t FROM SystemMessageTranslation t " +
           "WHERE t.language LIKE CONCAT(:languagePrefix, '%')")
    List<SystemMessageTranslation> findByLanguageStartingWith(@Param("languagePrefix") String languagePrefix);

    /**
     * Check if translation exists
     * <p>Fast existence check without loading entity</p>
     *
     * @param messageId SystemMessage ID
     * @param language Language code
     * @return true if translation exists
     */
    boolean existsByMessageIdAndLanguage(UUID messageId, String language);

    /**
     * Count translations for a language
     * <p>For monitoring and statistics</p>
     *
     * @param language Language code
     * @return Count of translations
     */
    long countByLanguage(String language);

    /**
     * Delete all translations for a specific language
     * <p>Use with caution - for admin bulk operations only</p>
     *
     * @param language Language code to delete
     * @return Number of deleted records
     */
    long deleteByLanguage(String language);

    /**
     * Find translations by category and language
     * <p>Optimized for category-specific translation loading</p>
     *
     * <p><strong>Use case:</strong></p>
     * Load only "button" translations for Russian language
     *
     * @param category Message category
     * @param language Language code
     * @return List of translations
     */
    @Query("SELECT t FROM SystemMessageTranslation t " +
           "JOIN t.systemMessage m " +
           "WHERE m.category = :category " +
           "AND t.language = :language " +
           "AND m.deletedAt IS NULL " +
           "AND m.isActive = true")
    List<SystemMessageTranslation> findByCategoryAndLanguage(
        @Param("category") String category,
        @Param("language") String language
    );
}
