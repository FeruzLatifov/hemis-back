package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.SystemMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SystemMessage Entity
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Data access layer for system messages</li>
 *   <li>Spring Data JPA auto-implementation</li>
 *   <li>Custom query methods for specific use cases</li>
 * </ul>
 *
 * <p><strong>UNIVER Pattern:</strong></p>
 * <ul>
 *   <li>Similar to SystemMessageRepository in UNIVER (Yii2)</li>
 *   <li>Supports category-based filtering</li>
 *   <li>Message key lookup for translations</li>
 *   <li>Active-only filtering</li>
 * </ul>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * // Find by message key
 * Optional&lt;SystemMessage&gt; message = repository.findByMessageKey("button.save");
 *
 * // Find all active messages in category
 * List&lt;SystemMessage&gt; buttons = repository.findByCategoryAndIsActiveTrue("button");
 *
 * // Find all with translations
 * List&lt;SystemMessage&gt; all = repository.findAllWithTranslations();
 * </pre>
 *
 * @see SystemMessage
 * @since 2.0.0
 */
@Repository
public interface SystemMessageRepository extends JpaRepository<SystemMessage, UUID>, JpaSpecificationExecutor<SystemMessage> {

    /**
     * Find system message by unique message key
     * <p>Returns active (non-deleted) message only due to @Where clause</p>
     *
     * @param messageKey Unique message key (e.g., "button.save")
     * @return Optional of SystemMessage
     */
    Optional<SystemMessage> findByMessageKey(String messageKey);

    /**
     * Find all messages by category
     * <p>Returns active messages only</p>
     *
     * @param category Message category (app, menu, button, label, etc.)
     * @return List of SystemMessage
     */
    List<SystemMessage> findByCategory(String category);

    /**
     * Find all active messages by category
     * <p>Explicitly filter by is_active = true</p>
     *
     * @param category Message category
     * @return List of active SystemMessage
     */
    List<SystemMessage> findByCategoryAndIsActiveTrue(String category);

    /**
     * Find all active messages
     * <p>For bulk loading in Redis cache</p>
     *
     * @return List of all active SystemMessage
     */
    List<SystemMessage> findByIsActiveTrue();

    /**
     * Find all messages with translations eagerly loaded
     * <p>Optimized for bulk operations - avoids N+1 query problem</p>
     *
     * <p><strong>Performance:</strong></p>
     * <ul>
     *   <li>Uses LEFT JOIN FETCH for eager loading</li>
     *   <li>Single query instead of N+1</li>
     *   <li>Ideal for caching all translations at startup</li>
     * </ul>
     *
     * @return List of SystemMessage with translations loaded
     */
    @Query("SELECT DISTINCT m FROM SystemMessage m " +
           "LEFT JOIN FETCH m.translations " +
           "WHERE m.deletedAt IS NULL AND m.isActive = true")
    List<SystemMessage> findAllWithTranslations();

    /**
     * Find messages by category with translations eagerly loaded
     * <p>Optimized for category-specific caching</p>
     *
     * @param category Message category
     * @return List of SystemMessage with translations
     */
    @Query("SELECT DISTINCT m FROM SystemMessage m " +
           "LEFT JOIN FETCH m.translations " +
           "WHERE m.category = :category " +
           "AND m.deletedAt IS NULL " +
           "AND m.isActive = true")
    List<SystemMessage> findByCategoryWithTranslations(@Param("category") String category);

    /**
     * Find message by key with translations eagerly loaded
     * <p>Single query with all translations</p>
     *
     * @param messageKey Unique message key
     * @return Optional of SystemMessage with translations
     */
    @Query("SELECT m FROM SystemMessage m " +
           "LEFT JOIN FETCH m.translations " +
           "WHERE m.messageKey = :messageKey " +
           "AND m.deletedAt IS NULL")
    Optional<SystemMessage> findByMessageKeyWithTranslations(@Param("messageKey") String messageKey);

    /**
     * Find message by ID with translations eagerly loaded
     *
     * @param id Message UUID
     * @return Optional of SystemMessage with translations
     */
    @Query("SELECT m FROM SystemMessage m " +
           "LEFT JOIN FETCH m.translations " +
           "WHERE m.id = :id " +
           "AND m.deletedAt IS NULL")
    Optional<SystemMessage> findByIdWithTranslations(@Param("id") UUID id);

    /**
     * Check if message key exists
     * <p>Fast existence check without loading entity</p>
     *
     * @param messageKey Message key to check
     * @return true if exists
     */
    boolean existsByMessageKey(String messageKey);

    /**
     * Count active messages by category
     * <p>For statistics and monitoring</p>
     *
     * @param category Message category
     * @return Count of active messages
     */
    long countByCategoryAndIsActiveTrue(String category);

    /**
     * Find message keys by category
     * <p>Returns only keys, not full entities - optimized query</p>
     *
     * @param category Message category
     * @return List of message keys
     */
    @Query("SELECT m.messageKey FROM SystemMessage m " +
           "WHERE m.category = :category " +
           "AND m.deletedAt IS NULL " +
           "AND m.isActive = true")
    List<String> findMessageKeysByCategory(@Param("category") String category);

    /**
     * Count messages by active status
     * <p>For admin statistics</p>
     *
     * @param isActive Active status
     * @return Count of messages
     */
    long countByIsActive(boolean isActive);

    /**
     * Count messages grouped by category
     * <p>For admin dashboard statistics</p>
     *
     * @return List of [category, count] tuples
     */
    @Query("SELECT m.category, COUNT(m) FROM SystemMessage m " +
           "WHERE m.deletedAt IS NULL " +
           "GROUP BY m.category " +
           "ORDER BY m.category")
    List<Object[]> countByCategory();
}
