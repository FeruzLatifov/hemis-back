package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.DiplomaBlank;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Diploma Blank Repository
 *
 * CRITICAL - NO DELETE OPERATIONS:
 * - Physical DELETE is prohibited (NDG - Non-Deletion Guarantee)
 * - Use soft delete (update delete_ts) in service layer
 *
 * Legacy API: /app/rest/diplom-blank/get, /app/rest/diplom-blank/setStatus
 */
@Repository
public interface DiplomaBlankRepository extends JpaRepository<DiplomaBlank, UUID> {

    // =====================================================
    // Query Methods (Spring Data JPA)
    // =====================================================

    Optional<DiplomaBlank> findByBlankCode(String blankCode);

    Optional<DiplomaBlank> findBySeriesAndNumber(String series, String number);

    Page<DiplomaBlank> findByUniversity(String universityCode, Pageable pageable);

    List<DiplomaBlank> findByUniversity(String universityCode);

    @Query("SELECT db FROM DiplomaBlank db WHERE db.university = :universityCode AND db.status = :status")
    Page<DiplomaBlank> findByUniversityAndStatus(
            @Param("universityCode") String universityCode,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT db FROM DiplomaBlank db WHERE db.university = :universityCode AND db.status = :status")
    List<DiplomaBlank> findByUniversityAndStatus(
            @Param("universityCode") String universityCode,
            @Param("status") String status
    );

    @Query("SELECT db FROM DiplomaBlank db WHERE db.university = :universityCode AND db.academicYear = :year")
    Page<DiplomaBlank> findByUniversityAndYear(
            @Param("universityCode") String universityCode,
            @Param("year") Integer year,
            Pageable pageable
    );

    @Query("SELECT db FROM DiplomaBlank db WHERE db.university = :universityCode AND db.blankType = :type")
    Page<DiplomaBlank> findByUniversityAndType(
            @Param("universityCode") String universityCode,
            @Param("type") String type,
            Pageable pageable
    );

    @Query("SELECT db FROM DiplomaBlank db WHERE db.university = :universityCode AND db.status = 'AVAILABLE' AND db.blankType = :type")
    List<DiplomaBlank> findAvailableByUniversityAndType(
            @Param("universityCode") String universityCode,
            @Param("type") String type
    );

    @Query("SELECT COUNT(db) FROM DiplomaBlank db WHERE db.university = :universityCode AND db.status = :status")
    long countByUniversityAndStatus(
            @Param("universityCode") String universityCode,
            @Param("status") String status
    );

    @Query("SELECT COUNT(db) FROM DiplomaBlank db WHERE db.university = :universityCode AND db.academicYear = :year AND db.status = 'AVAILABLE'")
    long countAvailableByUniversityAndYear(
            @Param("universityCode") String universityCode,
            @Param("year") Integer year
    );

    @Query("SELECT db FROM DiplomaBlank db WHERE db.series = :series ORDER BY db.number")
    List<DiplomaBlank> findBySeries(@Param("series") String series);

    boolean existsByBlankCode(String blankCode);

    // =====================================================
    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
    // =====================================================
}
