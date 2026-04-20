package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.AdministrativeEmployee3;

import java.util.List;
import java.util.UUID;

/**
 * Administrative Employee3 Repository
 *
 * DSc professors
 * Note: university, educationYear - bazada VARCHAR sifatida saqlangan (OLD-HEMIS compatibility)
 */
@Repository
@Transactional(readOnly = true)
public interface AdministrativeEmployee3Repository extends JpaRepository<AdministrativeEmployee3, UUID> {

    /**
     * Find by university
     */
    List<AdministrativeEmployee3> findByUniversity(String university);

    /**
     * Find by university and education year
     */
    List<AdministrativeEmployee3> findByUniversityAndEducationYear(String university, String educationYear);

    /**
     * Find by university and education year (paginated)
     */
    Page<AdministrativeEmployee3> findByUniversityAndEducationYear(String university, String educationYear, Pageable pageable);

    /**
     * Count by university and education year
     */
    long countByUniversityAndEducationYear(String university, String educationYear);
}
