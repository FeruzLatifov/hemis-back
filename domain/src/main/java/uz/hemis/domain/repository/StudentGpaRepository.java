package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.StudentGpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for StudentGpa entity
 *
 * <p>CRUD operations for hemishe_e_student_gpa table</p>
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * @see StudentGpa
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface StudentGpaRepository extends JpaRepository<StudentGpa, UUID> {

    /**
     * Find all GPA records by student ID
     *
     * @param studentId Student UUID
     * @return List of GPA records
     */
    List<StudentGpa> findByStudentId(UUID studentId);

    /**
     * Find GPA by student ID and education year
     *
     * @param studentId Student UUID
     * @param educationYearCode Education year code (e.g., "2023")
     * @return Optional GPA record
     */
    Optional<StudentGpa> findByStudentIdAndEducationYearCode(UUID studentId, String educationYearCode);

    /**
     * Find all GPA records for students in a specific university
     *
     * <p>Joins with hemishe_e_student to filter by university</p>
     *
     * @param universityCode University code
     * @param pageable Pagination info
     * @return Page of GPA records
     */
    @Query("""
        SELECT g FROM StudentGpa g
        JOIN Student s ON g.studentId = s.id
        WHERE s.university = :universityCode
        ORDER BY g.id DESC
        """)
    Page<StudentGpa> findByUniversityCode(@Param("universityCode") String universityCode, Pageable pageable);

    /**
     * Count GPA records for a specific university
     *
     * @param universityCode University code
     * @return Count of GPA records
     */
    @Query("""
        SELECT COUNT(g) FROM StudentGpa g
        JOIN Student s ON g.studentId = s.id
        WHERE s.university = :universityCode
        """)
    long countByUniversityCode(@Param("universityCode") String universityCode);

    /**
     * Find GPA records by education year
     *
     * @param educationYearCode Education year code
     * @param pageable Pagination info
     * @return Page of GPA records
     */
    Page<StudentGpa> findByEducationYearCode(String educationYearCode, Pageable pageable);

    /**
     * Check if GPA record exists for student and education year
     *
     * @param studentId Student UUID
     * @param educationYearCode Education year code
     * @return true if exists
     */
    boolean existsByStudentIdAndEducationYearCode(UUID studentId, String educationYearCode);
}
