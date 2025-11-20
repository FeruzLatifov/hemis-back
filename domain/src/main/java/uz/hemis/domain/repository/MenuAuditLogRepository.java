package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.MenuAuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Menu Audit Log Repository
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Query audit logs for menu changes</li>
 *   <li>Support admin UI (who changed what)</li>
 *   <li>Compliance and security reports</li>
 * </ul>
 *
 * <p><strong>Performance:</strong></p>
 * <ul>
 *   <li>Indexed queries: menu_id, action, changed_by, changed_at</li>
 *   <li>Paginated queries for large datasets</li>
 *   <li>Read-only transactions for all queries</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface MenuAuditLogRepository extends JpaRepository<MenuAuditLog, UUID> {

    // =====================================================
    // Query by Menu ID
    // =====================================================

    /**
     * Find all audit logs for specific menu item
     *
     * <p>Ordered by changed_at DESC (newest first)</p>
     *
     * @param menuId Menu ID
     * @return List of audit logs
     */
    @Query("SELECT a FROM MenuAuditLog a WHERE a.menuId = :menuId ORDER BY a.changedAt DESC")
    List<MenuAuditLog> findByMenuId(@Param("menuId") UUID menuId);

    /**
     * Find audit logs for menu (paginated)
     *
     * @param menuId Menu ID
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    Page<MenuAuditLog> findByMenuIdOrderByChangedAtDesc(UUID menuId, Pageable pageable);

    // =====================================================
    // Query by Action
    // =====================================================

    /**
     * Find all audit logs by action type
     *
     * @param action Action type (CREATE, UPDATE, DELETE, etc.)
     * @return List of audit logs
     */
    @Query("SELECT a FROM MenuAuditLog a WHERE a.action = :action ORDER BY a.changedAt DESC")
    List<MenuAuditLog> findByAction(@Param("action") String action);

    /**
     * Find audit logs by action (paginated)
     *
     * @param action Action type
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    Page<MenuAuditLog> findByActionOrderByChangedAtDesc(String action, Pageable pageable);

    // =====================================================
    // Query by User
    // =====================================================

    /**
     * Find all changes by specific user
     *
     * @param changedBy Username
     * @return List of audit logs
     */
    @Query("SELECT a FROM MenuAuditLog a WHERE a.changedBy = :changedBy ORDER BY a.changedAt DESC")
    List<MenuAuditLog> findByChangedBy(@Param("changedBy") String changedBy);

    /**
     * Find changes by user (paginated)
     *
     * @param changedBy Username
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    Page<MenuAuditLog> findByChangedByOrderByChangedAtDesc(String changedBy, Pageable pageable);

    // =====================================================
    // Query by Date Range
    // =====================================================

    /**
     * Find audit logs within date range
     *
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of audit logs
     */
    @Query("SELECT a FROM MenuAuditLog a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<MenuAuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find audit logs within date range (paginated)
     *
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Query("SELECT a FROM MenuAuditLog a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    Page<MenuAuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // =====================================================
    // Recent Changes
    // =====================================================

    /**
     * Find recent menu changes (last N days)
     *
     * @param since Date to search from
     * @return List of recent audit logs
     */
    @Query("SELECT a FROM MenuAuditLog a WHERE a.changedAt >= :since ORDER BY a.changedAt DESC")
    List<MenuAuditLog> findRecentChanges(@Param("since") LocalDateTime since);

    /**
     * Find recent changes (paginated)
     *
     * @param since Date to search from
     * @param pageable Pagination parameters
     * @return Page of audit logs
     */
    @Query("SELECT a FROM MenuAuditLog a WHERE a.changedAt >= :since ORDER BY a.changedAt DESC")
    Page<MenuAuditLog> findRecentChanges(@Param("since") LocalDateTime since, Pageable pageable);

    // =====================================================
    // Statistics
    // =====================================================

    /**
     * Count total audit logs
     *
     * @return Total count
     */
    @Query("SELECT COUNT(a) FROM MenuAuditLog a")
    long countAll();

    /**
     * Count audit logs by action
     *
     * @param action Action type
     * @return Count
     */
    long countByAction(String action);

    /**
     * Count changes by user
     *
     * @param changedBy Username
     * @return Count
     */
    long countByChangedBy(String changedBy);

    /**
     * Count changes within date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Count
     */
    @Query("SELECT COUNT(a) FROM MenuAuditLog a WHERE a.changedAt BETWEEN :startDate AND :endDate")
    long countByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // =====================================================
    // Admin Queries
    // =====================================================

    /**
     * Get all audit logs (admin UI)
     *
     * <p>Paginated for performance</p>
     *
     * @param pageable Pagination parameters
     * @return Page of all audit logs
     */
    @Query("SELECT a FROM MenuAuditLog a ORDER BY a.changedAt DESC")
    Page<MenuAuditLog> findAllForAdmin(Pageable pageable);

    /**
     * Find latest change for menu
     *
     * <p>Returns most recent audit log entry</p>
     *
     * @param menuId Menu ID
     * @return Latest audit log or empty
     */
    @Query("SELECT a FROM MenuAuditLog a WHERE a.menuId = :menuId ORDER BY a.changedAt DESC LIMIT 1")
    MenuAuditLog findLatestByMenuId(@Param("menuId") UUID menuId);

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // Audit logs are IMMUTABLE - never delete or update
    // For compliance: all changes must be permanently recorded
    // =====================================================
}
