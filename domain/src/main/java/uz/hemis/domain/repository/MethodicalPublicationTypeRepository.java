package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.MethodicalPublicationType;

import java.util.List;
import java.util.Optional;

/**
 * Uslubiy nashr turlari repository
 */
@Repository
public interface MethodicalPublicationTypeRepository extends JpaRepository<MethodicalPublicationType, String> {

    List<MethodicalPublicationType> findAllByIsActiveTrue();

    Optional<MethodicalPublicationType> findByCodeAndIsActiveTrue(String code);

    boolean existsByCodeAndIsActiveTrue(String code);

    /**
     * Find by code including inactive records (for upsert/restore logic)
     */
    @Query(value = "SELECT * FROM hemishe_h_methodical_publication_type WHERE code = :code", nativeQuery = true)
    Optional<MethodicalPublicationType> findByCodeIncludingDeleted(@Param("code") String code);
}
