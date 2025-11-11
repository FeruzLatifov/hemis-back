package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Teacher;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Teacher entity
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong></p>
 * <ul>
 *   <li>NO delete() methods defined</li>
 *   <li>NO deleteById() methods defined</li>
 *   <li>NO deleteAll() methods defined</li>
 *   <li>Soft delete ONLY (set deleteTs in service layer)</li>
 * </ul>
 *
 * <p><strong>Soft Delete Filtering:</strong></p>
 * <ul>
 *   <li>@Where(clause = "delete_ts IS NULL") on Teacher entity</li>
 *   <li>All queries automatically filter deleted records</li>
 * </ul>
 *
 * @see Teacher
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    // =====================================================
    // NO DELETE METHODS
    // =====================================================
    // Physical DELETE operations are PROHIBITED (NDG).
    // Use service layer soft delete (set deleteTs) instead.
    // =====================================================

    // =====================================================
    // PINFL Queries (Composite Unique: PINFL + University)
    // =====================================================
    // CRITICAL: In old-HEMIS, Teacher has composite unique constraint:
    //   UNIQUE (PINFL, _UNIVERSITY)
    // This means:
    //   - Same person (PINFL) can teach at MULTIPLE universities
    //   - But CANNOT have duplicate records at SAME university
    //   - PINFL alone is NOT unique globally
    // =====================================================

    /**
     * Find teacher by PINFL and University (composite key)
     *
     * <p><strong>CRITICAL:</strong> This is the correct way to look up teachers!</p>
     * <p>PINFL is unique PER UNIVERSITY, not globally.</p>
     * <p>Same person can teach at multiple universities.</p>
     *
     * @param pinfl personal identification number
     * @param universityCode university code
     * @return teacher if found at this university
     */
    @Query("SELECT t FROM Teacher t WHERE t.pinfl = :pinfl AND t.university = :universityCode")
    java.util.Optional<Teacher> findByPinflAndUniversity(
            @Param("pinfl") String pinfl,
            @Param("universityCode") String universityCode
    );

    /**
     * Find first teacher by PINFL (for backward compatibility)
     *
     * <p><strong>WARNING:</strong> May return ANY of multiple teachers with same PINFL!</p>
     * <p>Prefer {@link #findByPinflAndUniversity(String, String)} for precise lookup.</p>
     *
     * @param pinfl personal identification number
     * @return first teacher found (arbitrary if multiple exist)
     */
    @Query("SELECT t FROM Teacher t WHERE t.pinfl = :pinfl ORDER BY t.createTs DESC")
    java.util.Optional<Teacher> findByPinfl(@Param("pinfl") String pinfl);

    /**
     * Find ALL teachers with same PINFL (across all universities)
     *
     * <p><strong>Use case:</strong> Finding teaching history across universities</p>
     * <p>Same person can teach at multiple universities simultaneously.</p>
     *
     * @param pinfl personal identification number
     * @return list of all teacher records with this PINFL
     */
    @Query("SELECT t FROM Teacher t WHERE t.pinfl = :pinfl ORDER BY t.createTs DESC")
    List<Teacher> findAllByPinfl(@Param("pinfl") String pinfl);

    /**
     * Check if teacher exists at specific university (composite check)
     *
     * <p><strong>Use this for validation!</strong> NOT simple pinfl check.</p>
     *
     * @param pinfl personal identification number
     * @param universityCode university code
     * @return true if teacher exists at this university
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM Teacher t WHERE t.pinfl = :pinfl AND t.university = :universityCode")
    boolean existsByPinflAndUniversity(
            @Param("pinfl") String pinfl,
            @Param("universityCode") String universityCode
    );

    /**
     * Count how many universities this teacher works at
     *
     * @param pinfl personal identification number
     * @return count of universities
     */
    @Query("SELECT COUNT(DISTINCT t.university) FROM Teacher t WHERE t.pinfl = :pinfl")
    long countUniversitiesByPinfl(@Param("pinfl") String pinfl);

    // =====================================================
    // Find by University
    // =====================================================

    /**
     * Find teachers by university code
     *
     * @param universityCode university code
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.university = :universityCode")
    Page<Teacher> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    /**
     * Find all teachers by university code (no pagination)
     *
     * @param universityCode university code
     * @return List of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.university = :universityCode")
    List<Teacher> findAllByUniversity(@Param("universityCode") String universityCode);

    /**
     * Count teachers by university
     *
     * @param universityCode university code
     * @return count of teachers
     */
    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.university = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

    // =====================================================
    // Find by Name
    // =====================================================

    /**
     * Find teachers by lastname (partial match, case-insensitive)
     *
     * @param lastname last name (partial)
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE LOWER(t.lastname) LIKE LOWER(CONCAT('%', :lastname, '%'))")
    Page<Teacher> findByLastnameContainingIgnoreCase(@Param("lastname") String lastname, Pageable pageable);

    /**
     * Find teachers by full name (partial match on any name field, case-insensitive)
     *
     * @param name search term (applied to firstname, lastname, fathername)
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE " +
           "LOWER(t.firstname) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(t.lastname) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(t.fathername) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Teacher> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    // =====================================================
    // Find by Academic Qualifications
    // =====================================================

    /**
     * Find teachers by academic degree
     *
     * @param degreeCode academic degree code
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.academicDegree = :degreeCode")
    Page<Teacher> findByAcademicDegree(@Param("degreeCode") String degreeCode, Pageable pageable);

    /**
     * Find teachers by academic rank
     *
     * @param rankCode academic rank code
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.academicRank = :rankCode")
    Page<Teacher> findByAcademicRank(@Param("rankCode") String rankCode, Pageable pageable);

    /**
     * Find teachers by university and academic degree
     *
     * @param universityCode university code
     * @param degreeCode academic degree code
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.university = :universityCode AND t.academicDegree = :degreeCode")
    Page<Teacher> findByUniversityAndAcademicDegree(
            @Param("universityCode") String universityCode,
            @Param("degreeCode") String degreeCode,
            Pageable pageable
    );

    /**
     * Find teachers by university and academic rank
     *
     * @param universityCode university code
     * @param rankCode academic rank code
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.university = :universityCode AND t.academicRank = :rankCode")
    Page<Teacher> findByUniversityAndAcademicRank(
            @Param("universityCode") String universityCode,
            @Param("rankCode") String rankCode,
            Pageable pageable
    );

    // =====================================================
    // Find by Gender
    // =====================================================

    /**
     * Find teachers by gender
     *
     * @param genderCode gender code
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.gender = :genderCode")
    Page<Teacher> findByGender(@Param("genderCode") String genderCode, Pageable pageable);

    /**
     * Find teachers by university and gender
     *
     * @param universityCode university code
     * @param genderCode gender code
     * @param pageable pagination
     * @return Page of teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.university = :universityCode AND t.gender = :genderCode")
    Page<Teacher> findByUniversityAndGender(
            @Param("universityCode") String universityCode,
            @Param("genderCode") String genderCode,
            Pageable pageable
    );

    // =====================================================
    // Statistics
    // =====================================================

    /**
     * Count teachers with academic degree
     *
     * @return count of teachers with degree
     */
    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.academicDegree IS NOT NULL")
    long countTeachersWithDegree();

    /**
     * Count teachers with academic rank
     *
     * @return count of teachers with rank
     */
    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.academicRank IS NOT NULL")
    long countTeachersWithRank();

    /**
     * Count professors (academic rank = '14')
     *
     * @return count of professors
     */
    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.academicRank = '14'")
    long countProfessors();

    /**
     * Count professors by university
     *
     * @param universityCode university code
     * @return count of professors
     */
    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.university = :universityCode AND t.academicRank = '14'")
    long countProfessorsByUniversity(@Param("universityCode") String universityCode);

    // =====================================================
    // Custom Queries
    // =====================================================

    /**
     * Find professors (academic rank = '14') by university
     *
     * @param universityCode university code
     * @param pageable pagination
     * @return Page of professors
     */
    @Query("SELECT t FROM Teacher t WHERE t.university = :universityCode AND t.academicRank = '14'")
    Page<Teacher> findProfessorsByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    /**
     * Find teachers with doctorate degree (academic degree = '12' or '13')
     *
     * @param universityCode university code
     * @param pageable pagination
     * @return Page of teachers with doctorate
     */
    @Query("SELECT t FROM Teacher t WHERE t.university = :universityCode " +
           "AND t.academicDegree IN ('12', '13')")
    Page<Teacher> findDoctorsByUniversity(@Param("universityCode") String universityCode, Pageable pageable);
}
