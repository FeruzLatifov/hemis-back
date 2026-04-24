package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.security.OAuthClientRole;
import uz.hemis.domain.entity.security.OAuthClientRoleId;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link OAuthClientRole} junction entity.
 *
 * <p>Direct access is rare — most code should mutate bindings via
 * {@link uz.hemis.domain.entity.security.OAuthClient#bindRole} /
 * {@link uz.hemis.domain.entity.security.OAuthClient#unbindRole}
 * which rely on {@code orphanRemoval}.</p>
 *
 * <p>This repository exists for admin bulk operations and audit queries.</p>
 *
 * @since 2.1.0
 */
@Repository
@Transactional(readOnly = true)
public interface OAuthClientRoleRepository extends JpaRepository<OAuthClientRole, OAuthClientRoleId> {

    /**
     * All role bindings for a given client.
     *
     * @param clientId OAuth client UUID
     */
    @Query("SELECT b FROM OAuthClientRole b WHERE b.client.id = :clientId")
    List<OAuthClientRole> findByClientId(@Param("clientId") UUID clientId);

    /**
     * All role bindings for a given role (useful when revoking a role globally).
     */
    @Query("SELECT b FROM OAuthClientRole b WHERE b.role.id = :roleId")
    List<OAuthClientRole> findByRoleId(@Param("roleId") UUID roleId);

    /**
     * Check whether a specific binding exists.
     */
    @Query("SELECT COUNT(b) > 0 FROM OAuthClientRole b " +
           "WHERE b.client.id = :clientId AND b.role.id = :roleId")
    boolean existsByClientIdAndRoleId(@Param("clientId") UUID clientId,
                                      @Param("roleId") UUID roleId);

    /**
     * Bulk delete of a binding — used only in admin revoke flow.
     * Prefer {@code client.unbindRole(role)} + save for standard flow.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OAuthClientRole b WHERE b.client.id = :clientId AND b.role.id = :roleId")
    int deleteByClientIdAndRoleId(@Param("clientId") UUID clientId,
                                  @Param("roleId") UUID roleId);
}
