package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Role Repository - Spring Data JPA
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for roles (SUPER_ADMIN, MINISTRY_ADMIN, etc.)</li>
 *   <li>Query active roles (delete_ts IS NULL)</li>
 *   <li>Role-permission relationship management</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All queries filter deleted records (delete_ts IS NULL)</li>
 *   <li>System roles (SUPER_ADMIN, MINISTRY_ADMIN) should not be deleted</li>
 *   <li>Soft delete only (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // =====================================================
    // Basic Queries
    // =====================================================

    /**
     * Find role by code
     *
     * <p>Code is unique (e.g., SUPER_ADMIN, MINISTRY_ADMIN, VIEWER)</p>
     *
     * @param code Role code
     * @return Role if found
     */
    @Query("SELECT r FROM Role r WHERE r.code = :code AND r.deletedAt IS NULL")
    Optional<Role> findByCode(@Param("code") String code);

    /**
     * Find role by name
     *
     * @param name Role name (human-readable)
     * @return Role if found
     */
    @Query("SELECT r FROM Role r WHERE r.name = :name AND r.deletedAt IS NULL")
    Optional<Role> findByName(@Param("name") String name);

    /**
     * Check if role code exists
     *
     * @param code Role code
     * @return true if exists
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.code = :code AND r.deletedAt IS NULL")
    boolean existsByCode(@Param("code") String code);

    // =====================================================
    // Active Role Queries
    // =====================================================

    /**
     * Find all active roles (not deleted, enabled)
     *
     * @return List of active roles
     */
    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL AND r.active = true ORDER BY r.name")
    List<Role> findAllActive();

    /**
     * Find active role by code
     *
     * @param code Role code
     * @return Role if found and active
     */
    @Query("SELECT r FROM Role r WHERE r.code = :code AND r.deletedAt IS NULL AND r.active = true")
    Optional<Role> findActiveByCode(@Param("code") String code);

    // =====================================================
    // Role Type Queries
    // =====================================================

    /**
     * Find all roles by type
     *
     * @param roleType Role type (SYSTEM, UNIVERSITY, CUSTOM)
     * @return List of roles
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = :roleType AND r.deletedAt IS NULL ORDER BY r.name")
    List<Role> findByRoleType(@Param("roleType") String roleType);

    /**
     * Find all system roles (SYSTEM type)
     *
     * @return List of system roles
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = 'SYSTEM' AND r.deletedAt IS NULL ORDER BY r.name")
    List<Role> findAllSystemRoles();

    /**
     * Find all university roles (UNIVERSITY type)
     *
     * @return List of university roles
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = 'UNIVERSITY' AND r.deletedAt IS NULL ORDER BY r.name")
    List<Role> findAllUniversityRoles();

    // =====================================================
    // Permission-Related Queries
    // =====================================================

    /**
     * Find all roles with specific permission
     *
     * @param permissionId Permission ID
     * @return List of roles that have this permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId AND r.deletedAt IS NULL")
    List<Role> findByPermissionId(@Param("permissionId") UUID permissionId);

    /**
     * Find all roles with specific permission code
     *
     * @param permissionCode Permission code (e.g., "students.view")
     * @return List of roles that have this permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.code = :permissionCode AND r.deletedAt IS NULL")
    List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);

    // =====================================================
    // Statistics Queries
    // =====================================================

    /**
     * Count active roles
     *
     * @return Number of active roles
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.deletedAt IS NULL AND r.active = true")
    long countActive();

    /**
     * Count roles by type
     *
     * @param roleType Role type
     * @return Number of roles
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.roleType = :roleType AND r.deletedAt IS NULL")
    long countByRoleType(@Param("roleType") String roleType);

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // Soft delete only (set deletedAt in service layer)
    // System roles should NEVER be deleted
    // =====================================================
}
