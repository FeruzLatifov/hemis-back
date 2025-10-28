package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Translation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Translation entity
 * Handles multi-language translation queries
 */
@Repository
public interface TranslationRepository extends JpaRepository<Translation, UUID> {

    /**
     * Find translation by key
     */
    @Query("SELECT t FROM Translation t WHERE t.translationKey = :key AND t.isActive = true AND t.deletedDate IS NULL")
    Optional<Translation> findByKey(@Param("key") String key);

    /**
     * Find all translations for a module
     */
    @Query("SELECT t FROM Translation t WHERE t.module = :module AND t.isActive = true AND t.deletedDate IS NULL ORDER BY t.translationKey")
    List<Translation> findByModule(@Param("module") String module);

    /**
     * Find all active translations
     */
    @Query("SELECT t FROM Translation t WHERE t.isActive = true AND t.deletedDate IS NULL ORDER BY t.module, t.translationKey")
    List<Translation> findAllActive();

    /**
     * Search translations by text in any language
     */
    @Query("SELECT t FROM Translation t WHERE t.isActive = true AND t.deletedDate IS NULL AND " +
           "(LOWER(t.textUz) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.textRu) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.textEn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY t.translationKey")
    List<Translation> searchByText(@Param("searchTerm") String searchTerm);
}
