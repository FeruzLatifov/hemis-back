package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.academic.AcademicEducationalWork;

import java.util.UUID;

/**
 * Repository for AcademicEducationalWork entity
 *
 * O'quv ishlari
 */
@Repository
@Transactional(readOnly = true)
public interface AcademicEducationalWorkRepository extends JpaRepository<AcademicEducationalWork, UUID> {
}
