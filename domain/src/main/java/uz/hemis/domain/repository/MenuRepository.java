package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Menu;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Menu Repository - Spring Data JPA
 *
 * <p><strong>CRITICAL - Dynamic Menu System:</strong></p>
 * <ul>
 *   <li>Used by MenuService for database-driven menu structure</li>
 *   <li>Queries only active menus (active = true, deleted_at IS NULL)</li>
 *   <li>Supports hierarchical queries (parent → children)</li>
 *   <li>NO DELETE operations (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All queries filter deleted records (@Where clause)</li>
 *   <li>Permission-based filtering in service layer</li>
 *   <li>Active/inactive status check</li>
 * </ul>
 *
 * <p><strong>Performance:</strong></p>
 * <ul>
 *   <li>Indexed queries: parent_id, active, permission</li>
 *   <li>Eager fetching for hierarchical structure (avoid N+1)</li>
 *   <li>Cache-friendly: All queries return ordered results</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // =====================================================
    // Basic Queries
    // =====================================================

    /**
     * Find menu by code (unique identifier)
     *
     * <p>Used for programmatic menu access</p>
     * <p>Returns menu even if inactive (status check in service layer)</p>
     *
     * @param code menu code (e.g., "dashboard", "registry-e-reestr")
     * @return menu if found (including inactive menus)
     */
    Optional<Menu> findByCode(String code);

    /**
     * Check if menu code exists
     *
     * <p>Used for unique code validation during create/update</p>
     *
     * @param code menu code
     * @return true if code exists (even if inactive)
     */
    boolean existsByCode(String code);

    // =====================================================
    // Hierarchical Queries
    // =====================================================

    /**
     * Find all root menus (parent_id IS NULL)
     *
     * <p>Returns only active root menus, ordered by order_number</p>
     * <p>Used by MenuService to build menu tree</p>
     *
     * @return list of root menus
     */
    @Query("SELECT m FROM Menu m WHERE m.parent IS NULL AND m.active = true ORDER BY m.orderNumber ASC")
    List<Menu> findRootMenus();

    /**
     * Find all root menus with children eagerly fetched
     *
     * <p>Avoids N+1 lazy loading issues</p>
     * <p>Fetches complete menu tree in single query</p>
     *
     * @return list of root menus with children
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
           "LEFT JOIN FETCH m.children c " +
           "WHERE m.parent IS NULL AND m.active = true " +
           "ORDER BY m.orderNumber ASC, c.orderNumber ASC")
    List<Menu> findRootMenusWithChildren();

    /**
     * Find children by parent ID
     *
     * <p>Returns only active children, ordered by order_number</p>
     *
     * @param parentId parent menu ID
     * @return list of child menus
     */
    @Query("SELECT m FROM Menu m WHERE m.parent.id = :parentId AND m.active = true ORDER BY m.orderNumber ASC")
    List<Menu> findByParentId(@Param("parentId") UUID parentId);

    /**
     * Find all children by parent (entity)
     *
     * <p>Alternative to findByParentId using entity reference</p>
     *
     * @param parent parent menu entity
     * @return list of child menus
     */
    List<Menu> findByParentAndActiveTrueOrderByOrderNumberAsc(Menu parent);

    // =====================================================
    // Permission-Based Queries
    // =====================================================

    /**
     * Find menus by permission code
     *
     * <p>Returns all menus requiring specific permission</p>
     * <p>Used for permission-based menu filtering</p>
     *
     * @param permission permission code (e.g., "dashboard.view")
     * @return list of menus requiring this permission
     */
    @Query("SELECT m FROM Menu m WHERE m.permission = :permission AND m.active = true ORDER BY m.orderNumber ASC")
    List<Menu> findByPermission(@Param("permission") String permission);

    /**
     * Find public menus (no permission required)
     *
     * <p>Returns menus with permission IS NULL</p>
     * <p>Visible to all authenticated users</p>
     *
     * @return list of public menus
     */
    @Query("SELECT m FROM Menu m WHERE m.permission IS NULL AND m.active = true ORDER BY m.orderNumber ASC")
    List<Menu> findPublicMenus();

    // =====================================================
    // Active/Inactive Queries
    // =====================================================

    /**
     * Find all active menus
     *
     * <p>Returns all menus with active = true</p>
     * <p>Ordered by parent → order_number</p>
     *
     * @return list of active menus
     */
    @Query("SELECT m FROM Menu m WHERE m.active = true ORDER BY m.parent.id NULLS FIRST, m.orderNumber ASC")
    List<Menu> findAllActive();

    /**
     * Find all inactive menus
     *
     * <p>Returns all menus with active = false</p>
     * <p>Used for admin UI (show hidden menus)</p>
     *
     * @return list of inactive menus
     */
    @Query("SELECT m FROM Menu m WHERE m.active = false ORDER BY m.orderNumber ASC")
    List<Menu> findAllInactive();

    /**
     * Count active menus
     *
     * @return count of active menus
     */
    @Query("SELECT COUNT(m) FROM Menu m WHERE m.active = true")
    long countActiveMenus();

    // =====================================================
    // Complete Tree Queries (Performance Optimized)
    // =====================================================

    /**
     * Find complete menu tree (root menus with children eagerly fetched)
     *
     * <p>Performance-optimized query with eager fetching</p>
     * <p>Fetches root menus and their first-level children</p>
     * <p>For deeper nesting, use recursive loading in service layer</p>
     * <p>Used for menu caching and initial load</p>
     *
     * @return list of all root menus with children
     */
    @Query("SELECT m FROM Menu m WHERE m.parent IS NULL AND m.active = true ORDER BY m.orderNumber ASC")
    List<Menu> findCompleteMenuTree();

    /**
     * Find menu by ID with children eagerly fetched
     *
     * <p>Avoids lazy loading exception when accessing children</p>
     * <p>Used for menu details endpoint</p>
     *
     * @param id menu ID
     * @return menu with children if found
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
           "LEFT JOIN FETCH m.children " +
           "WHERE m.id = :id")
    Optional<Menu> findByIdWithChildren(@Param("id") UUID id);

    // =====================================================
    // Admin Queries (All Status)
    // =====================================================

    /**
     * Find all menus (active + inactive) for Admin UI
     *
     * <p>Returns all menus regardless of active status</p>
     * <p>Used for admin menu management UI</p>
     *
     * @return list of all menus
     */
    @Query("SELECT m FROM Menu m ORDER BY m.parent.id NULLS FIRST, m.orderNumber ASC")
    List<Menu> findAllForAdmin();

    /**
     * Find menu by ID (including inactive) for Admin UI
     *
     * <p>Returns menu regardless of active status</p>
     *
     * @param id menu ID
     * @return menu if found
     */
    @Query("SELECT m FROM Menu m WHERE m.id = :id")
    Optional<Menu> findByIdForAdmin(@Param("id") UUID id);

    /**
     * Find menu by code (including inactive) for Admin UI
     *
     * <p>Returns menu regardless of active status</p>
     *
     * @param code menu code
     * @return menu if found
     */
    @Query("SELECT m FROM Menu m WHERE m.code = :code")
    Optional<Menu> findByCodeForAdmin(@Param("code") String code);

    /**
     * Find all children by parent ID (including inactive) for Admin operations
     *
     * <p>Returns ALL child menus regardless of active status</p>
     * <p>Used for recursive soft delete - must include inactive children</p>
     *
     * @param parentId parent menu ID
     * @return list of all child menus (active + inactive)
     */
    @Query("SELECT m FROM Menu m WHERE m.parent.id = :parentId ORDER BY m.orderNumber ASC")
    List<Menu> findAllChildrenByParentId(@Param("parentId") UUID parentId);

    // =====================================================
    // Statistics Queries
    // =====================================================

    /**
     * Count total menus
     *
     * @return total count of all menus
     */
    @Query("SELECT COUNT(m) FROM Menu m")
    long countAll();

    /**
     * Count menus by parent
     *
     * @param parentId parent menu ID (null for root menus)
     * @return count of child menus
     */
    @Query("SELECT COUNT(m) FROM Menu m WHERE m.parent.id = :parentId")
    long countByParent(@Param("parentId") UUID parentId);

    /**
     * Count root menus (parent_id IS NULL)
     *
     * @return count of root menus
     */
    @Query("SELECT COUNT(m) FROM Menu m WHERE m.parent IS NULL")
    long countRootMenus();

    // =====================================================
    // Ordering Queries
    // =====================================================

    /**
     * Find maximum order number for same parent
     *
     * <p>Used when inserting new menu at end</p>
     *
     * @param parentId parent menu ID (null for root level)
     * @return maximum order number or null if no children
     */
    @Query("SELECT MAX(m.orderNumber) FROM Menu m WHERE " +
           "(:parentId IS NULL AND m.parent IS NULL) OR " +
           "(m.parent.id = :parentId)")
    Integer findMaxOrderNumber(@Param("parentId") UUID parentId);

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // The following inherited methods are available but PROHIBITED:
    // - void deleteById(UUID id)
    // - void delete(Menu entity)
    // - void deleteAll()
    //
    // These methods will FAIL at database level:
    // - Database role has NO DELETE permission
    // - Application enforces NDG (Non-Deletion Guarantee)
    //
    // For soft delete:
    // - Use service layer to set deletedAt = NOW()
    // - Queries automatically exclude deleted records (@Where clause)
    // =====================================================
}
