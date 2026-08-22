package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.UniversityBuilding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link UniversityBuilding}.
 * Har bir method muayyan service chaqiruvidan keladi (YAGNI).
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityBuildingRepository
        extends JpaRepository<UniversityBuilding, UUID>,
                JpaSpecificationExecutor<UniversityBuilding> {

    /** UniversityBuildingService.findByUniversity() chaqiradi. */
    @EntityGraph(attributePaths = {"buildingType", "ownership"})
    Page<UniversityBuilding> findByUniversityCode(String universityCode, Pageable pageable);

    /** Dashboard aggregate — barcha binolar bitta OTM uchun (paginate'siz). */
    @EntityGraph(attributePaths = {"buildingType", "ownership"})
    List<UniversityBuilding> findByUniversityCodeOrderByNameAsc(String universityCode);

    /** UniversityBuildingSyncService upsert uchun. */
    Optional<UniversityBuilding> findByUniversityCodeAndSourceUid(
            String universityCode, String sourceUid);
}
