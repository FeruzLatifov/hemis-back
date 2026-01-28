package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.EducationMaterials;

import java.util.List;
import java.util.UUID;

/**
 * EducationMaterials Repository
 *
 * PHASE 5: Infrastructure
 * Standard Spring Data JPA repository with custom query methods
 */
@Repository
@Transactional(readOnly = true)
public interface EducationMaterialsRepository extends JpaRepository<EducationMaterials, UUID> {

    /**
     * Find all education materials by university
     *
     * @param university University UUID
     * @return List of education materials
     */
    List<EducationMaterials> findByUniversity(UUID university);

    /**
     * Find education materials by university and education year
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @return List of education materials
     */
    List<EducationMaterials> findByUniversityAndEducationYear(UUID university, UUID educationYear);

    /**
     * Find education materials by university and education year (paginated)
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @param pageable Pagination parameters
     * @return Page of education materials
     */
    Page<EducationMaterials> findByUniversityAndEducationYear(UUID university, UUID educationYear, Pageable pageable);

    /**
     * Count education materials by university and education year
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @return Count
     */
    long countByUniversityAndEducationYear(UUID university, UUID educationYear);
}
