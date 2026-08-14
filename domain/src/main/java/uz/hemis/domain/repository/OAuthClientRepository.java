package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.auth.ClientType;
import uz.hemis.domain.entity.security.OAuthClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link OAuthClient} — B2B machine accounts.
 *
 * <p><strong>Used by:</strong></p>
 * <ul>
 *   <li>{@code OAuthClientAuthenticationService} — client_credentials grant validation</li>
 *   <li>{@code TokenService#issueClientToken} — token issuance</li>
 *   <li>Admin UI — client management (list, suspend, rotate secret)</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All queries filter {@code deleted_at IS NULL} via {@code @SQLRestriction}</li>
 *   <li>Client secret hash never exposed in read-only DTOs</li>
 *   <li>No physical DELETE — soft delete via {@code deletedAt}</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Repository
@Transactional(readOnly = true)
public interface OAuthClientRepository extends JpaRepository<OAuthClient, UUID> {

    // =====================================================
    // Authentication Queries (hot path — client_credentials grant)
    // =====================================================

    /**
     * Find client by {@code client_id} (OAuth public identifier).
     *
     * @param clientId public client identifier (e.g. {@code "univer_101"})
     * @return client if found (even if suspended)
     */
    Optional<OAuthClient> findByClientId(String clientId);

    /**
     * Find active, non-expired client with role bindings and permissions eagerly loaded.
     *
     * <p>Hot path for client_credentials grant — single query loads everything
     * needed to issue a token (avoids N+1 on role → permission).</p>
     *
     * @param clientId public client identifier
     * @return operational client with roles / permissions
     */
    @Query("SELECT DISTINCT c FROM OAuthClient c " +
           "LEFT JOIN FETCH c.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE c.clientId = :clientId " +
           "AND c.isActive = true " +
           "AND (c.expiresAt IS NULL OR c.expiresAt > CURRENT_TIMESTAMP)")
    Optional<OAuthClient> findOperationalByClientIdWithPermissions(@Param("clientId") String clientId);

    /**
     * Find client by ID with roles eagerly loaded.
     *
     * @param id client UUID
     * @return client with roles
     */
    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT c FROM OAuthClient c WHERE c.id = :id")
    Optional<OAuthClient> findByIdWithRoles(@Param("id") UUID id);

    // =====================================================
    // Lookup / Existence
    // =====================================================

    boolean existsByClientId(String clientId);

    /**
     * Find clients by type.
     *
     * @param clientType UNIVERSITY_BACKEND | EXTERNAL_SYSTEM | INTERNAL_SERVICE
     */
    List<OAuthClient> findByClientType(ClientType clientType);

    /**
     * All active clients belonging to a university (by university code).
     *
     * @param universityCode {@code hemishe_e_university.code}
     */
    @Query("SELECT c FROM OAuthClient c WHERE c.university.code = :universityCode " +
           "AND c.isActive = true ORDER BY c.clientName")
    List<OAuthClient> findActiveByUniversityCode(@Param("universityCode") String universityCode);

    // =====================================================
    // Admin list (filtered + paged)
    // =====================================================

    /**
     * Filtered + paged client list for the OTM API-client admin screen.
     *
     * @param search      matches clientId OR clientName (empty string = no filter)
     * @param clientType  exact type, or {@code null} for no filter
     * @param university  university code, or empty string for no filter
     * @param active      {@code is_active} value, or {@code null} for no filter
     */
    @EntityGraph(attributePaths = {"university"})
    @Query("SELECT c FROM OAuthClient c " +
           "WHERE (:search = '' OR LOWER(c.clientId) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "       OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:university = '' OR c.university.code = :university) " +
           "AND (:active IS NULL OR c.isActive = :active)")
    Page<OAuthClient> findAllFiltered(@Param("search") String search,
                                      @Param("university") String university,
                                      @Param("active") Boolean active,
                                      Pageable pageable);

    // =====================================================
    // Lifecycle Queries
    // =====================================================

    /**
     * Find clients whose secrets have rotated more than {@code days} ago
     * (for rotation reminder cron job).
     *
     * @param threshold timestamp cut-off
     */
    @Query("SELECT c FROM OAuthClient c WHERE c.isActive = true " +
           "AND (c.secretRotatedAt IS NULL OR c.secretRotatedAt < :threshold)")
    List<OAuthClient> findClientsDueForRotation(@Param("threshold") LocalDateTime threshold);

    /**
     * Find clients that expired but are still marked active
     * (nightly cleanup job disables them).
     *
     * @param now current timestamp
     */
    @Query("SELECT c FROM OAuthClient c WHERE c.isActive = true " +
           "AND c.expiresAt IS NOT NULL AND c.expiresAt < :now")
    List<OAuthClient> findExpiredActive(@Param("now") LocalDateTime now);

    // =====================================================
    // Statistics
    // =====================================================

    @Query("SELECT COUNT(c) FROM OAuthClient c WHERE c.isActive = true")
    long countActive();

    @Query("SELECT COUNT(c) FROM OAuthClient c WHERE c.clientType = :type AND c.isActive = true")
    long countActiveByType(@Param("type") ClientType type);

    // =====================================================
    // Atomic Updates (avoid OptimisticLockException on hot path)
    // =====================================================

    /**
     * Atomically update {@code last_used_at} + {@code last_used_ip} on successful token issuance.
     * Bypasses JPA optimistic locking to avoid contention on high-traffic clients.
     *
     * @param id client UUID
     * @param ip remote IP
     * @return rows updated
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE oauth_client SET last_used_at = NOW(), last_used_ip = :ip " +
                   "WHERE id = :id AND deleted_at IS NULL",
           nativeQuery = true)
    int updateLastUsed(@Param("id") UUID id, @Param("ip") String ip);

    // =====================================================
    // NOTE: NO DELETE METHODS (soft delete only)
    // =====================================================
}
