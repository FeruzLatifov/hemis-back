package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Laboratories;

import java.util.List;
import java.util.UUID;

/**
 * Laboratories Repository
 *
 * PHASE 5: Infrastructure
 * Standard Spring Data JPA repository with custom query methods
 */
@Repository
@Transactional(readOnly = true)
public interface LaboratoriesRepository extends JpaRepository<Laboratories, UUID> {

    /**
     * Find all laboratories by university
     *
     * @param university University UUID
     * @return List of laboratories
     */
    List<Laboratories> findByUniversity(UUID university);

    /**
     * Find laboratories by university and education year
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @return List of laboratories
     */
    List<Laboratories> findByUniversityAndEducationYear(UUID university, UUID educationYear);

    /**
     * Find laboratories by university and education year (paginated)
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @param pageable Pagination parameters
     * @return Page of laboratories
     */
    Page<Laboratories> findByUniversityAndEducationYear(UUID university, UUID educationYear, Pageable pageable);

    /**
     * Count laboratories by university and education year
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @return Count
     */
    long countByUniversityAndEducationYear(UUID university, UUID educationYear);
}
