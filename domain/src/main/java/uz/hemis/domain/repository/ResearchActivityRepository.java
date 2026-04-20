package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.research.ResearchActivity;

import java.util.List;
import java.util.UUID;

/**
 * Research Activity repository.
 *
 * Note: university, educationYear, scholarDatabase are String (VARCHAR) codes,
 * not UUIDs. They are foreign keys to their respective tables' "code" columns.
 */
@Repository
@Transactional(readOnly = true)
public interface ResearchActivityRepository extends JpaRepository<ResearchActivity, UUID> {

    List<ResearchActivity> findByUniversity(String university);

    List<ResearchActivity> findByUniversityAndEducationYear(String university, String educationYear);

    Page<ResearchActivity> findByUniversityAndEducationYear(String university, String educationYear, Pageable pageable);

    long countByUniversityAndEducationYear(String university, String educationYear);
}
