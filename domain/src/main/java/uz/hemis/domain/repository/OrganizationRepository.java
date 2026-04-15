package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hemis.domain.entity.Organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByTin(String tin);
    boolean existsByTin(String tin);
}
