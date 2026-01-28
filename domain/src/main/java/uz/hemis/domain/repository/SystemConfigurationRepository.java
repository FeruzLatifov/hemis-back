package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.SystemConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * System Configuration Repository - UNIVER Pattern
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for system configurations</li>
 *   <li>Find configurations by category</li>
 *   <li>Path-based lookups</li>
 * </ul>
 *
 * @see SystemConfiguration
 * @since 2.0.0
 */
@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, UUID> {

    /**
     * Find configuration by path
     *
     * @param path Configuration path (e.g., system.language.uz-UZ)
     * @return Optional configuration
     */
    Optional<SystemConfiguration> findByPath(String path);

    /**
     * Find all configurations by category
     *
     * @param category Configuration category (e.g., system, language, security)
     * @return List of configurations
     */
    List<SystemConfiguration> findByCategoryOrderByPathAsc(String category);

    /**
     * Find all language-related configurations
     *
     * @return List of language configurations
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.path LIKE 'system.language.%' ORDER BY c.path ASC")
    List<SystemConfiguration> findAllLanguageConfigurations();

    /**
     * Check if configuration exists
     *
     * @param path Configuration path
     * @return true if exists
     */
    boolean existsByPath(String path);

    /**
     * Find all editable configurations
     *
     * @return List of editable configurations
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isEditable = true ORDER BY c.category ASC, c.path ASC")
    List<SystemConfiguration> findAllEditable();
}
