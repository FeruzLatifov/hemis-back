package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Course entity
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong></p>
 * <ul>
 *   <li>NO delete() methods defined</li>
 *   <li>NO deleteById() methods defined</li>
 *   <li>NO deleteAll() methods defined</li>
 *   <li>Soft delete ONLY (set deleteTs in service layer)</li>
 * </ul>
 *
 * @see Course
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface CourseRepository extends JpaRepository<Course, UUID> {

    // NO DELETE METHODS (NDG)

    /**
     * Find course by code
     */
    Optional<Course> findByCode(String code);

    /**
     * Find courses by university code
     */
    @Query("SELECT c FROM Course c WHERE c.university = :universityCode")
    Page<Course> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    /**
     * Find all courses by university (no pagination)
     */
    @Query("SELECT c FROM Course c WHERE c.university = :universityCode")
    List<Course> findAllByUniversity(@Param("universityCode") String universityCode);

    /**
     * Find courses by subject ID
     */
    @Query("SELECT c FROM Course c WHERE c.subject = :subjectId")
    Page<Course> findBySubject(@Param("subjectId") UUID subjectId, Pageable pageable);

    /**
     * Find courses by name (partial match, case-insensitive)
     */
    @Query("SELECT c FROM Course c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Course> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find active courses
     */
    @Query("SELECT c FROM Course c WHERE c.active = true")
    Page<Course> findByActiveTrue(Pageable pageable);

    /**
     * Find courses by course type
     */
    @Query("SELECT c FROM Course c WHERE c.courseType = :typeCode")
    Page<Course> findByCourseType(@Param("typeCode") String typeCode, Pageable pageable);

    /**
     * Find courses by assessment type
     */
    @Query("SELECT c FROM Course c WHERE c.assessmentType = :assessmentCode")
    Page<Course> findByAssessmentType(@Param("assessmentCode") String assessmentCode, Pageable pageable);

    /**
     * Find elective courses
     */
    @Query("SELECT c FROM Course c WHERE c.isElective = true")
    Page<Course> findElectiveCourses(Pageable pageable);

    /**
     * Find courses by semester
     */
    @Query("SELECT c FROM Course c WHERE c.semester = :semester")
    Page<Course> findBySemester(@Param("semester") Integer semester, Pageable pageable);

    /**
     * Find courses by university and semester
     */
    @Query("SELECT c FROM Course c WHERE c.university = :universityCode AND c.semester = :semester")
    Page<Course> findByUniversityAndSemester(
            @Param("universityCode") String universityCode,
            @Param("semester") Integer semester,
            Pageable pageable
    );

    /**
     * Count courses by university
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.university = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

    /**
     * Count courses by subject
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.subject = :subjectId")
    long countBySubject(@Param("subjectId") UUID subjectId);

    /**
     * Check if course with code exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE c.code = :code")
    boolean existsByCode(@Param("code") String code);

    /**
     * Check if course with code exists (excluding current ID)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE c.code = :code AND c.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);
}
