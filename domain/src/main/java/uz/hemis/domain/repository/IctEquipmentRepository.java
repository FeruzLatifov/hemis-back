package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.IctEquipment;

import java.util.List;
import java.util.UUID;

/**
 * IctEquipment Repository
 *
 * PHASE 5: Infrastructure
 * Standard Spring Data JPA repository with custom query methods
 */
@Repository
@Transactional(readOnly = true)
public interface IctEquipmentRepository extends JpaRepository<IctEquipment, UUID> {

    /**
     * Find all ICT equipment by university
     *
     * @param university University UUID
     * @return List of ICT equipment
     */
    List<IctEquipment> findByUniversity(UUID university);

    /**
     * Find ICT equipment by university and education year
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @return List of ICT equipment
     */
    List<IctEquipment> findByUniversityAndEducationYear(UUID university, UUID educationYear);

    /**
     * Find ICT equipment by university and education year (paginated)
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @param pageable Pagination parameters
     * @return Page of ICT equipment
     */
    Page<IctEquipment> findByUniversityAndEducationYear(UUID university, UUID educationYear, Pageable pageable);

    /**
     * Count ICT equipment by university and education year
     *
     * @param university University UUID
     * @param educationYear Education year UUID
     * @return Count
     */
    long countByUniversityAndEducationYear(UUID university, UUID educationYear);
}
