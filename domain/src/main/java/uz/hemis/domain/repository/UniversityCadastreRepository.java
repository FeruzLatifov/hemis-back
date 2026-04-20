package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.UniversityCadastre;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UniversityCadastre entity
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for university cadastre records</li>
 *   <li>Query by university code, cadastre number, region, and district</li>
 * </ul>
 *
 * <p><strong>Soft Delete Filtering:</strong></p>
 * <ul>
 *   <li>@SQLRestriction("delete_ts IS NULL") on entity</li>
 *   <li>All queries automatically filter deleted records</li>
 * </ul>
 *
 * @see UniversityCadastre
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityCadastreRepository extends JpaRepository<UniversityCadastre, UUID> {

    List<UniversityCadastre> findByUniversityCode(String universityCode);

    Optional<UniversityCadastre> findByCadNumber(String cadNumber);

    boolean existsByCadNumber(String cadNumber);

    List<UniversityCadastre> findByRegionId(Integer regionId);

    List<UniversityCadastre> findByDistrictId(Integer districtId);
}
