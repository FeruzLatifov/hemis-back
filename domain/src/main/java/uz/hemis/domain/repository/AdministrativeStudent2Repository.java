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
 */
@Repository
@Transactional(readOnly = true)
public interface AdministrativeStudent2Repository extends JpaRepository<AdministrativeStudent2, UUID> {

    /**
     * Find by university
     */
    List<AdministrativeStudent2> findByUniversity(UUID university);

    /**
     * Find by university and education year
     */
    List<AdministrativeStudent2> findByUniversityAndEducationYear(UUID university, UUID educationYear);

    /**
     * Find by university and education year (paginated)
     */
    Page<AdministrativeStudent2> findByUniversityAndEducationYear(UUID university, UUID educationYear, Pageable pageable);

    /**
     * Count by university and education year
     */
    long countByUniversityAndEducationYear(UUID university, UUID educationYear);
}
