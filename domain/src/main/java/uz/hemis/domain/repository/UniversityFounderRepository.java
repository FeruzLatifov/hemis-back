package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.UniversityFounder;

import java.util.List;
import java.util.UUID;

/**
 * Repository for UniversityFounder entity
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for university founders</li>
 *   <li>Query current and historical founders by university code</li>
 *   <li>Query by PINFL and employee ID</li>
 * </ul>
 *
 * <p><strong>Soft Delete Filtering:</strong></p>
 * <ul>
 *   <li>@SQLRestriction("delete_ts IS NULL") on entity</li>
 *   <li>All queries automatically filter deleted records</li>
 * </ul>
 *
 * @see UniversityFounder
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityFounderRepository extends JpaRepository<UniversityFounder, UUID> {

    @EntityGraph(attributePaths = {"employee", "organization"})
    List<UniversityFounder> findByUniversityCode(String universityCode);

    @EntityGraph(attributePaths = {"employee", "organization"})
    List<UniversityFounder> findByUniversityCodeAndIsCurrent(String universityCode, boolean isCurrent);

    List<UniversityFounder> findByEmployee_Pinfl(String pinfl);

    List<UniversityFounder> findByEmployee_Id(UUID employeeId);

    long countByUniversityCodeAndIsCurrent(String universityCode, boolean isCurrent);
}
