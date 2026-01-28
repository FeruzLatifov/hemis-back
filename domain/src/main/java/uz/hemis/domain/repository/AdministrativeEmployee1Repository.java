package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.AdministrativeEmployee1;

import java.util.List;
import java.util.UUID;

/**
 * Administrative Employee1 Repository
 *
 * PhD/DSc from top universities
 */
@Repository
@Transactional(readOnly = true)
public interface AdministrativeEmployee1Repository extends JpaRepository<AdministrativeEmployee1, UUID> {

    /**
     * Find by university
     */
    List<AdministrativeEmployee1> findByUniversity(String university);

    /**
     * Find by university and education year
     */
    List<AdministrativeEmployee1> findByUniversityAndEducationYear(String university, String educationYear);

    /**
     * Find by university and education year (paginated)
     */
    Page<AdministrativeEmployee1> findByUniversityAndEducationYear(String university, String educationYear, Pageable pageable);

    /**
     * Count by university and education year
     */
    long countByUniversityAndEducationYear(String university, String educationYear);
}
