package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.Organization;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByTin(String tin);
    boolean existsByTin(String tin);
}
