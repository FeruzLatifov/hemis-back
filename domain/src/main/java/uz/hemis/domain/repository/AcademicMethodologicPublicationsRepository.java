package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.AcademicMethodologicPublications;

import java.util.UUID;

/**
 * Repository for AcademicMethodologicPublications entity
 *
 * Uslubiy nashrlar
 */
@Repository
public interface AcademicMethodologicPublicationsRepository extends JpaRepository<AcademicMethodologicPublications, UUID> {
}
