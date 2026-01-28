package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Translation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Translation Repository
 *
 * @author System Architect
 */
@Repository
public interface TranslationRepository extends JpaRepository<Translation, UUID> {

    /**
     * Find translation by key and locale
     */
    Optional<Translation> findByKeyAndLocale(String key, String locale);

    /**
     * Find all translations for a specific locale
     */
    List<Translation> findAllByLocale(String locale);

    /**
     * Find all translations for a specific locale and category
     */
    List<Translation> findAllByLocaleAndCategory(String locale, String category);

    /**
     * Find translations by keys and locale
     */
    @Query("SELECT t FROM Translation t WHERE t.key IN :keys AND t.locale = :locale")
    List<Translation> findByKeysAndLocale(@Param("keys") List<String> keys, @Param("locale") String locale);

    /**
     * Get all translations as a Map for a specific locale
     */
    default Map<String, String> getTranslationsMap(String locale) {
        return findAllByLocale(locale).stream()
            .collect(Collectors.toMap(Translation::getKey, Translation::getValue));
    }

    /**
     * Get translations map by category
     */
    default Map<String, String> getTranslationsMapByCategory(String locale, String category) {
        return findAllByLocaleAndCategory(locale, category).stream()
            .collect(Collectors.toMap(Translation::getKey, Translation::getValue));
    }
}
