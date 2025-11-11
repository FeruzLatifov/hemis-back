package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.AdministrativeStudentSport;

import java.util.List;
import java.util.UUID;

/**
 * Administrative Student Sport Repository
 *
 * Sports achievements
 */
@Repository
@Transactional(readOnly = true)
public interface AdministrativeStudentSportRepository extends JpaRepository<AdministrativeStudentSport, UUID> {

    /**
     * Find by university
     */
    List<AdministrativeStudentSport> findByUniversity(UUID university);

    /**
     * Find by university and education year
     */
    List<AdministrativeStudentSport> findByUniversityAndEducationYear(UUID university, UUID educationYear);

    /**
     * Find by university and education year (paginated)
     */
    Page<AdministrativeStudentSport> findByUniversityAndEducationYear(UUID university, UUID educationYear, Pageable pageable);

    /**
     * Count by university and education year
     */
    long countByUniversityAndEducationYear(UUID university, UUID educationYear);
}
