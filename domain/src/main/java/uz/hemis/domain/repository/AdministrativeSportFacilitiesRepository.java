package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.AdministrativeSportFacilities;

import java.util.UUID;

/**
 * Repository for AdministrativeSportFacilities entity
 *
 * Sport inshootlari
 */
@Repository
@Transactional(readOnly = true)
public interface AdministrativeSportFacilitiesRepository extends JpaRepository<AdministrativeSportFacilities, UUID> {
}
