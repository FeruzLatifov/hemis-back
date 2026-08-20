package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hemis.domain.entity.infrastructure.HBuildingOwnership;

import java.util.List;

/**
 * Read access to the {@code h_building_ownership} classifier (PK = ownership code).
 * Dictionary for the egalik dropdown / distribution snapshot.
 */
public interface HBuildingOwnershipRepository extends JpaRepository<HBuildingOwnership, String> {

    List<HBuildingOwnership> findByIsActiveTrueOrderBySortOrderAscCodeAsc();
}
