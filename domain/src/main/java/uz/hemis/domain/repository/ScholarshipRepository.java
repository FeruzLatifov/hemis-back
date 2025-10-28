package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Scholarship;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Scholarship Repository
 *
 * CRITICAL - NO DELETE OPERATIONS:
 * - Physical DELETE is prohibited (NDG - Non-Deletion Guarantee)
 * - Use soft delete (update delete_ts) in service layer
 *
 * Legacy: Old-HEMIS ScholarshipService
 */
@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, UUID> {

    // =====================================================
    // Query Methods (Spring Data JPA)
    // =====================================================

    Optional<Scholarship> findByScholarshipCode(String scholarshipCode);

    List<Scholarship> findByStudent(UUID studentId);

    Page<Scholarship> findByStudent(UUID studentId, Pageable pageable);

    Page<Scholarship> findByUniversity(String universityCode, Pageable pageable);

    List<Scholarship> findByUniversity(String universityCode);

    @Query("SELECT s FROM Scholarship s WHERE s.university = :universityCode AND s.educationYear = :year")
    Page<Scholarship> findByUniversityAndYear(
            @Param("universityCode") String universityCode,
            @Param("year") String year,
            Pageable pageable
    );

    @Query("SELECT s FROM Scholarship s WHERE s.university = :universityCode AND s.educationYear = :year AND s.semester = :semester")
    Page<Scholarship> findByUniversityAndYearAndSemester(
            @Param("universityCode") String universityCode,
            @Param("year") String year,
            @Param("semester") Integer semester,
            Pageable pageable
    );

    @Query("SELECT s FROM Scholarship s WHERE s.university = :universityCode AND s.status = :status")
    Page<Scholarship> findByUniversityAndStatus(
            @Param("universityCode") String universityCode,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT s FROM Scholarship s WHERE s.student = :studentId AND s.status = 'ACTIVE' AND s.isActive = true")
    List<Scholarship> findActiveByStudent(@Param("studentId") UUID studentId);

    @Query("SELECT s FROM Scholarship s WHERE s.university = :universityCode AND s.status = 'ACTIVE' AND s.isActive = true")
    List<Scholarship> findActiveByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT COUNT(s) FROM Scholarship s WHERE s.university = :universityCode AND s.educationYear = :year AND s.status = 'ACTIVE'")
    long countActiveByUniversityAndYear(
            @Param("universityCode") String universityCode,
            @Param("year") String year
    );

    @Query("SELECT SUM(s.amount) FROM Scholarship s WHERE s.university = :universityCode AND s.educationYear = :year AND s.status = 'ACTIVE'")
    BigDecimal sumAmountByUniversityAndYear(
            @Param("universityCode") String universityCode,
            @Param("year") String year
    );

    boolean existsByScholarshipCode(String scholarshipCode);

    // =====================================================
    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
    // =====================================================
}
