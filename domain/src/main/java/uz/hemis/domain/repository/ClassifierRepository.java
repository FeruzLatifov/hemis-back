package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Classifier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Classifier entity
 * Handles reference/lookup data queries
 */
@Repository
public interface ClassifierRepository extends JpaRepository<Classifier, UUID> {

    /**
     * Find classifier by type and code
     * Example: type="STUDENT_STATUS", code="11" → Active student
     */
    @Query("SELECT c FROM Classifier c WHERE c.classifierType = :type AND c.code = :code AND c.isActive = true AND c.deletedDate IS NULL")
    Optional<Classifier> findByTypeAndCode(@Param("type") String type, @Param("code") String code);

    /**
     * Find all classifiers by type
     * Example: type="PAYMENT_FORM" → [Grant, Contract, Self-funded]
     */
    @Query("SELECT c FROM Classifier c WHERE c.classifierType = :type AND c.isActive = true AND c.deletedDate IS NULL ORDER BY c.sortOrder, c.nameUz")
    List<Classifier> findAllByType(@Param("type") String type);

    /**
     * Find all active classifiers
     */
    @Query("SELECT c FROM Classifier c WHERE c.isActive = true AND c.deletedDate IS NULL ORDER BY c.classifierType, c.sortOrder")
    List<Classifier> findAllActive();

    /**
     * Get all classifier types
     */
    @Query("SELECT DISTINCT c.classifierType FROM Classifier c WHERE c.isActive = true AND c.deletedDate IS NULL ORDER BY c.classifierType")
    List<String> findAllTypes();

    /**
     * Search classifiers by name (all languages)
     */
    @Query("SELECT c FROM Classifier c WHERE c.isActive = true AND c.deletedDate IS NULL AND " +
           "(LOWER(c.nameUz) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.nameRu) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY c.sortOrder")
    List<Classifier> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Count classifiers by type
     */
    @Query("SELECT COUNT(c) FROM Classifier c WHERE c.classifierType = :type AND c.isActive = true AND c.deletedDate IS NULL")
    long countByType(@Param("type") String type);
}
