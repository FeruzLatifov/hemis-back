package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.AcademicEducationalWork;

import java.util.UUID;

/**
 * Repository for AcademicEducationalWork entity
 *
 * O'quv ishlari
 */
@Repository
public interface AcademicEducationalWorkRepository extends JpaRepository<AcademicEducationalWork, UUID> {
}
