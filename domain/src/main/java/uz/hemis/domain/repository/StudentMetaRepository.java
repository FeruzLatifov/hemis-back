package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.StudentMeta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * StudentMeta Repository - Spring Data JPA
 *
 * <p><strong>CRITICAL - NO DELETE OPERATIONS:</strong></p>
 * <ul>
 *   <li>NDG (Non-Deletion Guarantee) - physical DELETE prohibited</li>
 *   <li>Inherited delete methods blocked at DB role level</li>
 *   <li>Soft delete handled at service layer (set deleteTs)</li>
 *   <li>All queries automatically filter deleted records (@Where clause)</li>
 * </ul>
 *
 * <p><strong>Read-Only Optimization:</strong></p>
 * <ul>
 *   <li>Most queries use @Transactional(readOnly=true)</li>
 *   <li>Routes to replica database (if Master-Replica configured)</li>
 *   <li>Better performance for SELECT queries</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface StudentMetaRepository extends JpaRepository<StudentMeta, UUID> {

    // =====================================================
    // Business Key Queries
    // =====================================================

    /**
     * Find student meta by university-specific unique ID and university code
     *
     * <p><strong>UNIQUE constraint:</strong> (uId, university) is unique</p>
     *
     * @param uId university-specific ID
     * @param university university code
     * @return student meta if found
     */
    @Query("SELECT sm FROM StudentMeta sm WHERE sm.uId = :uId AND sm.university = :university")
    Optional<StudentMeta> findByUIdAndUniversity(@Param("uId") Integer uId, @Param("university") String university);

    /**
     * Check if student meta exists by uId and university
     *
     * @param uId university-specific ID
     * @param university university code
     * @return true if exists
     */
    @Query("SELECT CASE WHEN COUNT(sm) > 0 THEN true ELSE false END FROM StudentMeta sm WHERE sm.uId = :uId AND sm.university = :university")
    boolean existsByUIdAndUniversity(@Param("uId") Integer uId, @Param("university") String university);

    // =====================================================
    // Student-based Queries
    // =====================================================

    /**
     * Find all meta records for a student
     *
     * @param studentId student UUID
     * @return list of meta records
     */
    List<StudentMeta> findByStudent(UUID studentId);

    /**
     * Find active meta records for a student
     *
     * @param studentId student UUID
     * @return list of active meta records
     */
    @Query("SELECT sm FROM StudentMeta sm WHERE sm.student = :studentId AND sm.active = true")
    List<StudentMeta> findActiveByStudent(@Param("studentId") UUID studentId);

    /**
     * Find latest meta record for a student
     *
     * @param studentId student UUID
     * @return latest meta record
     */
    @Query("SELECT sm FROM StudentMeta sm WHERE sm.student = :studentId ORDER BY sm.universityUpdatedAt DESC, sm.createTs DESC")
    List<StudentMeta> findLatestByStudent(@Param("studentId") UUID studentId);

    // =====================================================
    // University-based Queries
    // =====================================================

    /**
     * Find all student metas by university code
     *
     * @param universityCode university code
     * @return list of student metas
     */
    List<StudentMeta> findByUniversity(String universityCode);

    /**
     * Find all student metas by university code (paginated)
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return page of student metas
     */
    Page<StudentMeta> findByUniversity(String universityCode, Pageable pageable);

    /**
     * Find active student metas by university
     *
     * @param universityCode university code
     * @return list of active student metas
     */
    @Query("SELECT sm FROM StudentMeta sm WHERE sm.university = :universityCode AND sm.active = true")
    List<StudentMeta> findActiveByUniversity(@Param("universityCode") String universityCode);

    /**
     * Count student metas by university
     *
     * @param universityCode university code
     * @return count
     */
    long countByUniversity(String universityCode);

    // =====================================================
    // Education-based Queries
    // =====================================================

    /**
     * Find student metas by education year
     *
     * @param educationYear education year code
     * @return list of student metas
     */
    List<StudentMeta> findByEducationYear(String educationYear);

    /**
     * Find student metas by university and education year
     *
     * @param universityCode university code
     * @param educationYear education year code
     * @return list of student metas
     */
    List<StudentMeta> findByUniversityAndEducationYear(String universityCode, String educationYear);

    /**
     * Find student metas by student status
     *
     * @param studentStatus status code (e.g., '11' = active, '16' = graduated)
     * @return list of student metas
     */
    List<StudentMeta> findByStudentStatus(String studentStatus);

    /**
     * Find student metas by university and student status
     *
     * @param universityCode university code
     * @param studentStatus status code
     * @return list of student metas
     */
    List<StudentMeta> findByUniversityAndStudentStatus(String universityCode, String studentStatus);

    // =====================================================
    // Group-based Queries
    // =====================================================

    /**
     * Find student metas by group ID
     *
     * @param groupId group ID
     * @return list of student metas
     */
    List<StudentMeta> findByGroupId(Integer groupId);

    /**
     * Find student metas by group name
     *
     * @param groupName group name
     * @return list of student metas
     */
    List<StudentMeta> findByGroupName(String groupName);

    // =====================================================
    // Department-based Queries
    // =====================================================

    /**
     * Find student metas by department
     *
     * @param departmentId department UUID
     * @return list of student metas
     */
    List<StudentMeta> findByDepartment(UUID departmentId);

    /**
     * Count student metas by department
     *
     * @param departmentId department UUID
     * @return count
     */
    long countByDepartment(UUID departmentId);

    // =====================================================
    // Complex Queries
    // =====================================================

    /**
     * Find student metas by university, education year, and student status
     *
     * @param universityCode university code
     * @param educationYear education year code
     * @param studentStatus status code
     * @param pageable pagination
     * @return page of student metas
     */
    @Query("SELECT sm FROM StudentMeta sm WHERE sm.university = :university " +
           "AND sm.educationYear = :educationYear " +
           "AND sm.studentStatus = :studentStatus")
    Page<StudentMeta> findByUniversityAndEducationYearAndStudentStatus(
            @Param("university") String universityCode,
            @Param("educationYear") String educationYear,
            @Param("studentStatus") String studentStatus,
            Pageable pageable
    );

    /**
     * Find student metas for diploma registration
     *
     * @param universityCode university code
     * @return list of student metas with diploma registration
     */
    @Query("SELECT sm FROM StudentMeta sm WHERE sm.university = :university AND sm.diplomaRegistration IS NOT NULL")
    List<StudentMeta> findWithDiplomaRegistration(@Param("university") String universityCode);

    /**
     * Find student metas for employment registration
     *
     * @param universityCode university code
     * @return list of student metas with employment registration
     */
    @Query("SELECT sm FROM StudentMeta sm WHERE sm.university = :university AND sm.employmentRegistration IS NOT NULL")
    List<StudentMeta> findWithEmploymentRegistration(@Param("university") String universityCode);

    // =====================================================
    // Max uId Query (for ID generation)
    // =====================================================

    /**
     * Find max uId for a university (for generating next uId)
     *
     * @param universityCode university code
     * @return max uId or null if no records
     */
    @Query("SELECT MAX(sm.uId) FROM StudentMeta sm WHERE sm.university = :university")
    Integer findMaxUIdByUniversity(@Param("university") String universityCode);

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // NDG (Non-Deletion Guarantee) - no physical DELETE
    // Soft delete handled at service layer by setting deleteTs
    // =====================================================
}
