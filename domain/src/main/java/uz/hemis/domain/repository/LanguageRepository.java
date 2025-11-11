package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Language;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Language Repository - UNIVER Pattern
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for languages</li>
 *   <li>Find active languages for UI</li>
 *   <li>Custom ordering by position</li>
 * </ul>
 *
 * @see Language
 * @since 2.0.0
 */
@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {

    /**
     * Find language by code
     *
     * @param code Language code (e.g., uz-UZ, ru-RU)
     * @return Optional language
     */
    Optional<Language> findByCode(String code);

    /**
     * Find all active languages ordered by position
     * <p>Used for language selector in UI</p>
     *
     * @return List of active languages
     */
    @Query("SELECT l FROM Language l WHERE l.isActive = true ORDER BY l.position ASC, l.name ASC")
    List<Language> findAllActiveOrderedByPosition();

    /**
     * Find all languages ordered by position
     * <p>Used for admin panel</p>
     *
     * @return List of all languages
     */
    @Query("SELECT l FROM Language l ORDER BY l.position ASC, l.name ASC")
    List<Language> findAllOrderedByPosition();

    /**
     * Check if language code exists
     *
     * @param code Language code
     * @return true if exists
     */
    boolean existsByCode(String code);

    /**
     * Count active languages
     *
     * @return Number of active languages
     */
    long countByIsActiveTrue();

    /**
     * Find default languages (uz-UZ, oz-UZ, ru-RU)
     *
     * @return List of default languages
     */
    @Query("SELECT l FROM Language l WHERE l.code IN ('uz-UZ', 'oz-UZ', 'ru-RU') ORDER BY l.position ASC")
    List<Language> findDefaultLanguages();
}
