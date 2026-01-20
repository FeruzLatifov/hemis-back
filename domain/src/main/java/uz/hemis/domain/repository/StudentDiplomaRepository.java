package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.StudentDiploma;

import java.util.List;
import java.util.UUID;

/**
 * Repository for StudentDiploma (Talaba diplomlari)
 *
 * <p>Primary key: id (UUID)</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface StudentDiplomaRepository extends JpaRepository<StudentDiploma, UUID> {

    List<StudentDiploma> findByActiveTrue();

    List<StudentDiploma> findByUniversity(String university);

    List<StudentDiploma> findByStudent(UUID studentId);

    List<StudentDiploma> findByDiplomaNumberContainingIgnoreCase(String diplomaNumber);

    /**
     * Diploma raqami bo'yicha aniq qidirish
     */
    List<StudentDiploma> findByDiplomaNumber(String diplomaNumber);
}
