package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.EducationMaterials;

import java.util.List;
import java.util.UUID;

/**
 * EducationMaterials Repository
 *
 * PHASE 5: Infrastructure
 */
@Repository
@Transactional(readOnly = true)
public interface EducationMaterialsRepository extends JpaRepository<EducationMaterials, UUID> {

    List<EducationMaterials> findByUniversity(String university);

    List<EducationMaterials> findByUniversityAndEducationYear(String university, String educationYear);

    Page<EducationMaterials> findByUniversityAndEducationYear(String university, String educationYear, Pageable pageable);

    long countByUniversityAndEducationYear(String university, String educationYear);
}
