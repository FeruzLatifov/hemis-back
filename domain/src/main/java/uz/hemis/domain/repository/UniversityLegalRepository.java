package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.UniversityLegal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UniversityLegal entity
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for university legal information</li>
 *   <li>Query by university code, TIN, and SOATO</li>
 * </ul>
 *
 * <p><strong>Soft Delete Filtering:</strong></p>
 * <ul>
 *   <li>@SQLRestriction("delete_ts IS NULL") on entity</li>
 *   <li>All queries automatically filter deleted records</li>
 * </ul>
 *
 * @see UniversityLegal
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityLegalRepository extends JpaRepository<UniversityLegal, UUID> {

    @EntityGraph(attributePaths = {"directorEmployee", "accountantEmployee"})
    Optional<UniversityLegal> findByUniversityCode(String universityCode);

    Optional<UniversityLegal> findByTin(String tin);

    boolean existsByUniversityCode(String universityCode);

    List<UniversityLegal> findByBillingSoato(String soato);
}
