package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Scholarship;

import java.util.List;
import java.util.UUID;

/**
 * Scholarship Repository
 *
 * Table: hemishe_e_student_scholarship_full
 */
@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, UUID> {

    List<Scholarship> findByStudent(UUID studentId);

    List<Scholarship> findByUniversity(String universityCode);
}
