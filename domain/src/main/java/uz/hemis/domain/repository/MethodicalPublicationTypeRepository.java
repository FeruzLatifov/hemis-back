package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.research.MethodicalPublicationType;

import java.util.List;
import java.util.Optional;

/**
 * Uslubiy nashr turlari repository.
 *
 * <p>Entity extends {@code LegacyClassifierEntity} — the underlying column is
 * named {@code active} (Boolean). Spring Data derived queries use the name
 * {@code isActiveTrue} in method names for readability, but the JPQL below
 * references the actual property {@code active} to avoid
 * {@code PropertyReferenceException: No property 'isActive' found}.</p>
 */
@Repository
@Transactional(readOnly = true)
public interface MethodicalPublicationTypeRepository extends JpaRepository<MethodicalPublicationType, String> {

    @Query("SELECT c FROM MethodicalPublicationType c WHERE c.active = true")
    List<MethodicalPublicationType> findAllByIsActiveTrue();

    @Query("SELECT c FROM MethodicalPublicationType c WHERE c.code = :code AND c.active = true")
    Optional<MethodicalPublicationType> findByCodeAndIsActiveTrue(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM MethodicalPublicationType c WHERE c.code = :code AND c.active = true")
    boolean existsByCodeAndIsActiveTrue(@Param("code") String code);

    /**
     * Find by code including inactive records (for upsert/restore logic).
     * Yangi jadvalga yo'naltirilgan (Bosqich 4 refactor) — eski hemishe_h_* tegilmaydi.
     */
    @Query(value = "SELECT * FROM methodical_publication_type WHERE code = :code", nativeQuery = true)
    Optional<MethodicalPublicationType> findByCodeIncludingDeleted(@Param("code") String code);
}
