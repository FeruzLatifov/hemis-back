package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.BuildingLifecycle;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link BuildingLifecycle} immutable event log.
 * Service qatlamida: save() (INSERT) va findByBuildingId (tarix chiqarish).
 */
@Repository
@Transactional(readOnly = true)
public interface BuildingLifecycleRepository extends JpaRepository<BuildingLifecycle, UUID> {

    /** UniversityBuildingService.getHistory() chaqiradi. */
    List<BuildingLifecycle> findByBuildingIdOrderByEventDateDesc(UUID buildingId);
}
