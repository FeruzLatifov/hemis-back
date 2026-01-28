package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Diploma;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Diploma Repository
 *
 * CRITICAL - NO DELETE OPERATIONS:
 * - Physical DELETE is prohibited (NDG - Non-Deletion Guarantee)
 * - Use soft delete (update delete_ts) in service layer
 *
 * Legacy API: /app/rest/diploma/info, /app/rest/diploma/byhash
 */
@Repository
public interface DiplomaRepository extends JpaRepository<Diploma, UUID> {

    // =====================================================
    // Query Methods (Spring Data JPA)
    // =====================================================

    Optional<Diploma> findByDiplomaNumber(String diplomaNumber);

    Optional<Diploma> findByDiplomaHash(String diplomaHash);

    Optional<Diploma> findByStudent(UUID studentId);

    List<Diploma> findByStudentOrderByIssueDateDesc(UUID studentId);

    Page<Diploma> findByUniversity(String universityCode, Pageable pageable);

    List<Diploma> findByUniversity(String universityCode);

    Page<Diploma> findBySpecialty(UUID specialtyId, Pageable pageable);

    @Query("SELECT d FROM Diploma d WHERE d.university = :universityCode AND d.graduationYear = :year")
    Page<Diploma> findByUniversityAndYear(
            @Param("universityCode") String universityCode,
            @Param("year") Integer year,
            Pageable pageable
    );

    @Query("SELECT d FROM Diploma d WHERE d.university = :universityCode AND d.diplomaType = :type")
    Page<Diploma> findByUniversityAndType(
            @Param("universityCode") String universityCode,
            @Param("type") String type,
            Pageable pageable
    );

    @Query("SELECT d FROM Diploma d WHERE d.university = :universityCode AND d.status = :status")
    Page<Diploma> findByUniversityAndStatus(
            @Param("universityCode") String universityCode,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT d FROM Diploma d WHERE d.issueDate BETWEEN :startDate AND :endDate")
    List<Diploma> findByIssueDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(d) FROM Diploma d WHERE d.university = :universityCode AND d.graduationYear = :year")
    long countByUniversityAndYear(
            @Param("universityCode") String universityCode,
            @Param("year") Integer year
    );

    @Query("SELECT COUNT(d) FROM Diploma d WHERE d.university = :universityCode AND d.status = 'ISSUED'")
    long countIssuedByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT d FROM Diploma d WHERE d.diplomaBlank = :blankId")
    Optional<Diploma> findByDiplomaBlank(@Param("blankId") UUID blankId);

    boolean existsByDiplomaNumber(String diplomaNumber);

    boolean existsByDiplomaHash(String diplomaHash);

    // =====================================================
    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
    // =====================================================
}
