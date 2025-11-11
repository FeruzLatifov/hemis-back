package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.AdministrativeEmployee2;

import java.util.List;
import java.util.UUID;

/**
 * Administrative Employee2 Repository
 *
 * Professor internships
 */
@Repository
@Transactional(readOnly = true)
public interface AdministrativeEmployee2Repository extends JpaRepository<AdministrativeEmployee2, UUID> {

    /**
     * Find by university
     */
    List<AdministrativeEmployee2> findByUniversity(UUID university);

    /**
     * Find by university and education year
     */
    List<AdministrativeEmployee2> findByUniversityAndEducationYear(UUID university, UUID educationYear);

    /**
     * Find by university and education year (paginated)
     */
    Page<AdministrativeEmployee2> findByUniversityAndEducationYear(UUID university, UUID educationYear, Pageable pageable);

    /**
     * Count by university and education year
     */
    long countByUniversityAndEducationYear(UUID university, UUID educationYear);
}
