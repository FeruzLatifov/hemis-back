package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.AdministrativeStudent2;

import java.util.List;
import java.util.UUID;

/**
 * Administrative Student2 Repository
 *
 * Foreign university academic exchange programs (by students)
 *
 * Note: university and educationYear fields are VARCHAR in old-hemis database, not UUID.
 * Repository uses String for these fields to match Entity definition.
 */
@Repository
@Transactional(readOnly = true)
public interface AdministrativeStudent2Repository extends JpaRepository<AdministrativeStudent2, UUID> {

    /**
     * Find by university (String - VARCHAR in database)
     */
    List<AdministrativeStudent2> findByUniversity(String university);

    /**
     * Find by university and education year (String - VARCHAR in database)
     */
    List<AdministrativeStudent2> findByUniversityAndEducationYear(String university, String educationYear);

    /**
     * Find by university and education year (paginated)
     */
    Page<AdministrativeStudent2> findByUniversityAndEducationYear(String university, String educationYear, Pageable pageable);

    /**
     * Count by university and education year
     */
    long countByUniversityAndEducationYear(String university, String educationYear);
}
