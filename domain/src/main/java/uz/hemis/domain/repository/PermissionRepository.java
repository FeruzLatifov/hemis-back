package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Permission Repository - Spring Data JPA
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for permissions (students.view, reports.create, etc.)</li>
 *   <li>Query permissions by resource, action, category</li>
 *   <li>Permission-role relationship management</li>
 * </ul>
 *
 * <p><strong>Permission Format:</strong></p>
 * <ul>
 *   <li>Code: resource.action (e.g., "students.view", "reports.create")</li>
 *   <li>Resource: Entity/module name (students, teachers, reports)</li>
 *   <li>Action: Operation (view, create, edit, delete, manage)</li>
 * </ul>
 *
 * <p><strong>Categories:</strong></p>
 * <ul>
 *   <li>CORE - Essential business entities</li>
 *   <li>REPORTS - Reporting functionality</li>
 *   <li>ADMIN - Administrative features</li>
 *   <li>INTEGRATION - External API integrations</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    // =====================================================
    // Basic Queries
    // =====================================================

    /**
     * Find permission by code
     *
     * <p>Code is unique (e.g., "students.view", "reports.create")</p>
     *
     * @param code Permission code
     * @return Permission if found
     */
    @Query("SELECT p FROM Permission p WHERE p.code = :code AND p.deletedAt IS NULL")
    Optional<Permission> findByCode(@Param("code") String code);

    /**
     * Find permission by resource and action
     *
     * @param resource Resource name
     * @param action Action name
     * @return Permission if found
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.deletedAt IS NULL")
    Optional<Permission> findByResourceAndAction(
        @Param("resource") String resource,
        @Param("action") String action
    );

    /**
     * Check if permission code exists
     *
     * @param code Permission code
     * @return true if exists
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.code = :code AND p.deletedAt IS NULL")
    boolean existsByCode(@Param("code") String code);

    // =====================================================
    // Resource-Based Queries
    // =====================================================

    /**
     * Find all permissions for a resource
     *
     * @param resource Resource name (e.g., "students", "teachers")
     * @return List of permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.deletedAt IS NULL ORDER BY p.action")
    List<Permission> findByResource(@Param("resource") String resource);

    /**
     * Find all permissions by action
     *
     * @param action Action name (e.g., "view", "create", "edit")
     * @return List of permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.action = :action AND p.deletedAt IS NULL ORDER BY p.resource")
    List<Permission> findByAction(@Param("action") String action);

    /**
     * Find all view permissions (action = 'view')
     *
     * @return List of view permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.action = 'view' AND p.deletedAt IS NULL ORDER BY p.resource")
    List<Permission> findAllViewPermissions();

    // =====================================================
    // Category-Based Queries
    // =====================================================

    /**
     * Find all permissions by category
     *
     * @param category Category name (CORE, REPORTS, ADMIN, INTEGRATION)
     * @return List of permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.category = :category AND p.deletedAt IS NULL ORDER BY p.code")
    List<Permission> findByCategory(@Param("category") String category);

    /**
     * Find all core permissions (category = CORE)
     *
     * @return List of core permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.category = 'CORE' AND p.deletedAt IS NULL ORDER BY p.code")
    List<Permission> findAllCorePermissions();

    /**
     * Find all admin permissions (category = ADMIN)
     *
     * @return List of admin permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.category = 'ADMIN' AND p.deletedAt IS NULL ORDER BY p.code")
    List<Permission> findAllAdminPermissions();

    /**
     * Find all report permissions (category = REPORTS)
     *
     * @return List of report permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.category = 'REPORTS' AND p.deletedAt IS NULL ORDER BY p.code")
    List<Permission> findAllReportPermissions();

    // =====================================================
    // Role-Related Queries
    // =====================================================

    /**
     * Find all permissions for a specific role
     *
     * @param roleId Role ID
     * @return List of permissions
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId AND p.deletedAt IS NULL ORDER BY p.code")
    List<Permission> findByRoleId(@Param("roleId") UUID roleId);

    /**
     * Find all permissions for a specific role code
     *
     * @param roleCode Role code (e.g., "SUPER_ADMIN")
     * @return List of permissions
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.code = :roleCode AND p.deletedAt IS NULL ORDER BY p.code")
    List<Permission> findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * Find permissions NOT assigned to a specific role
     *
     * @param roleId Role ID
     * @return List of permissions not in this role
     */
    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NULL AND p.id NOT IN " +
           "(SELECT p2.id FROM Permission p2 JOIN p2.roles r WHERE r.id = :roleId) " +
           "ORDER BY p.code")
    List<Permission> findNotInRole(@Param("roleId") UUID roleId);

    // =====================================================
    // Search Queries
    // =====================================================

    /**
     * Search permissions by code or name
     *
     * @param keyword Search keyword
     * @return List of matching permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NULL AND " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.code")
    List<Permission> search(@Param("keyword") String keyword);

    // =====================================================
    // All Active Permissions
    // =====================================================

    /**
     * Find all active permissions (not deleted)
     *
     * @return List of all active permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NULL ORDER BY p.category, p.resource, p.action")
    List<Permission> findAllActive();

    // =====================================================
    // Statistics Queries
    // =====================================================

    /**
     * Count all active permissions
     *
     * @return Number of permissions
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.deletedAt IS NULL")
    long countActive();

    /**
     * Count permissions by category
     *
     * @param category Category name
     * @return Number of permissions
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.category = :category AND p.deletedAt IS NULL")
    long countByCategory(@Param("category") String category);

    /**
     * Count permissions by resource
     *
     * @param resource Resource name
     * @return Number of permissions
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.resource = :resource AND p.deletedAt IS NULL")
    long countByResource(@Param("resource") String resource);

    // =====================================================
    // Batch Operations
    // =====================================================

    /**
     * Find permissions by code list (batch lookup)
     *
     * @param codes List of permission codes
     * @return List of permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.code IN :codes AND p.deletedAt IS NULL")
    List<Permission> findByCodes(@Param("codes") List<String> codes);

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // Soft delete only (set deletedAt in service layer)
    // =====================================================
}
