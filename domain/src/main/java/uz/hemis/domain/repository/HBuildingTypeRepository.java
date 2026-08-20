package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hemis.domain.entity.infrastructure.HBuildingType;

import java.util.List;

/**
 * Read access to the modern {@code h_building_type} classifier (PK = building-type code).
 * Used to populate the "Bino turi" dropdown and to resolve/validate a type code;
 * distribution snapshot source (keyingi bosqich).
 */
public interface HBuildingTypeRepository extends JpaRepository<HBuildingType, String> {

    /** All active types, in display order — the dictionary for the FE dropdown / distribution snapshot. */
    List<HBuildingType> findByIsActiveTrueOrderBySortOrderAscCodeAsc();
}
