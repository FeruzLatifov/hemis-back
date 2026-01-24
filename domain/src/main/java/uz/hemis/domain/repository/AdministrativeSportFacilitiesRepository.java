package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.AdministrativeSportFacilities;

import java.util.UUID;

/**
 * Repository for AdministrativeSportFacilities entity
 *
 * Sport inshootlari
 */
@Repository
public interface AdministrativeSportFacilitiesRepository extends JpaRepository<AdministrativeSportFacilities, UUID> {
}
