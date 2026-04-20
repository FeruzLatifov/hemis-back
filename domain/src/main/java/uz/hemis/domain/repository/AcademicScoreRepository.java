package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.academic.AcademicScore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AcademicScore entity.
 * Provides CRUD operations with CUBA soft-delete pattern support.
 *
 * <p>CRITICAL - CUBA Compatibility:</p>
 * <ul>
 *   <li>Soft delete: Uses delete_ts column (entity has @SQLRestriction)</li>
 *   <li>All standard queries automatically filter deleted records</li>
 *   <li>Use softDelete() method for CUBA-compatible deletion</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Repository
public interface AcademicScoreRepository extends JpaRepository<AcademicScore, UUID>, JpaSpecificationExecutor<AcademicScore> {

    /**
     * Find by university code.
     *
     * @param universityCode the university code
     * @return list of academic scores
     */
    List<AcademicScore> findByUniversityCode(String universityCode);

    /**
     * Find by faculty code.
     *
     * @param facultyCode the faculty code
     * @return list of academic scores
     */
    List<AcademicScore> findByFacultyCode(String facultyCode);

    /**
     * Find by education year code.
     *
     * @param educationYearCode the education year code
     * @return list of academic scores
     */
    List<AcademicScore> findByEducationYearCode(String educationYearCode);

    /**
     * Find by table type.
     *
     * @param tableType the table type
     * @return list of academic scores
     */
    List<AcademicScore> findByTableType(String tableType);

    /**
     * Soft delete by ID (CUBA pattern).
     * Sets delete_ts and deleted_by instead of physically deleting.
     *
     * @param id the entity ID
     * @param deletedBy the user who performed the deletion
     * @param deleteTs the deletion timestamp
     * @return number of affected rows
     */
    @Modifying
    @Query("UPDATE AcademicScore e SET e.deleteTs = :deleteTs, e.deletedBy = :deletedBy WHERE e.id = :id")
    int softDelete(@Param("id") UUID id, @Param("deletedBy") String deletedBy, @Param("deleteTs") LocalDateTime deleteTs);

    /**
     * Find including soft-deleted records.
     *
     * @param id the entity ID
     * @return optional academic score
     */
    @Query("SELECT e FROM AcademicScore e WHERE e.id = :id")
    Optional<AcademicScore> findByIdIncludingDeleted(@Param("id") UUID id);
}
