package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.Laboratories;

import java.util.List;
import java.util.UUID;

/**
 * Laboratories Repository
 *
 * PHASE 5: Infrastructure
 */
@Repository
@Transactional(readOnly = true)
public interface LaboratoriesRepository extends JpaRepository<Laboratories, UUID> {

    List<Laboratories> findByUniversity(String university);

    List<Laboratories> findByUniversityAndEducationYear(String university, String educationYear);

    Page<Laboratories> findByUniversityAndEducationYear(String university, String educationYear, Pageable pageable);

    long countByUniversityAndEducationYear(String university, String educationYear);
}
