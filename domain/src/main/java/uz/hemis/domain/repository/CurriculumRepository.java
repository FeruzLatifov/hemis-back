package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Curriculum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Curriculum entity
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong></p>
 * <ul>
 *   <li>NO delete() methods defined</li>
 *   <li>NO deleteById() methods defined</li>
 *   <li>NO deleteAll() methods defined</li>
 *   <li>Soft delete ONLY (set deleteTs in service layer)</li>
 * </ul>
 *
 * @see Curriculum
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface CurriculumRepository extends JpaRepository<Curriculum, UUID> {

    // NO DELETE METHODS (NDG)

    /**
     * Find curriculum by code
     */
    Optional<Curriculum> findByCode(String code);

    /**
     * Find curricula by university code
     */
    @Query("SELECT c FROM Curriculum c WHERE c.university = :universityCode")
    Page<Curriculum> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    /**
     * Find all curricula by university (no pagination)
     */
    @Query("SELECT c FROM Curriculum c WHERE c.university = :universityCode")
    List<Curriculum> findAllByUniversity(@Param("universityCode") String universityCode);

    /**
     * Find curricula by specialty ID
     */
    @Query("SELECT c FROM Curriculum c WHERE c.specialty = :specialtyId")
    Page<Curriculum> findBySpecialty(@Param("specialtyId") UUID specialtyId, Pageable pageable);

    /**
     * Find all curricula by specialty (no pagination)
     */
    @Query("SELECT c FROM Curriculum c WHERE c.specialty = :specialtyId")
    List<Curriculum> findAllBySpecialty(@Param("specialtyId") UUID specialtyId);

    /**
     * Find curricula by name (partial match, case-insensitive)
     */
    @Query("SELECT c FROM Curriculum c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Curriculum> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find active curricula
     */
    @Query("SELECT c FROM Curriculum c WHERE c.active = true")
    Page<Curriculum> findByActiveTrue(Pageable pageable);

    /**
     * Find approved curricula
     */
    @Query("SELECT c FROM Curriculum c WHERE c.isApproved = true")
    Page<Curriculum> findApprovedCurricula(Pageable pageable);

    /**
     * Find curricula by academic year
     */
    @Query("SELECT c FROM Curriculum c WHERE c.academicYear = :year")
    Page<Curriculum> findByAcademicYear(@Param("year") String year, Pageable pageable);

    /**
     * Find curricula by education type
     */
    @Query("SELECT c FROM Curriculum c WHERE c.educationType = :typeCode")
    Page<Curriculum> findByEducationType(@Param("typeCode") String typeCode, Pageable pageable);

    /**
     * Find curricula by education form
     */
    @Query("SELECT c FROM Curriculum c WHERE c.educationForm = :formCode")
    Page<Curriculum> findByEducationForm(@Param("formCode") String formCode, Pageable pageable);

    /**
     * Find curricula by curriculum type
     */
    @Query("SELECT c FROM Curriculum c WHERE c.curriculumType = :typeCode")
    Page<Curriculum> findByCurriculumType(@Param("typeCode") String typeCode, Pageable pageable);

    /**
     * Find curricula by university and academic year
     */
    @Query("SELECT c FROM Curriculum c WHERE c.university = :universityCode AND c.academicYear = :year")
    Page<Curriculum> findByUniversityAndAcademicYear(
            @Param("universityCode") String universityCode,
            @Param("year") String year,
            Pageable pageable
    );

    /**
     * Find curricula by specialty and academic year
     */
    @Query("SELECT c FROM Curriculum c WHERE c.specialty = :specialtyId AND c.academicYear = :year")
    Page<Curriculum> findBySpecialtyAndAcademicYear(
            @Param("specialtyId") UUID specialtyId,
            @Param("year") String year,
            Pageable pageable
    );

    /**
     * Count curricula by university
     */
    @Query("SELECT COUNT(c) FROM Curriculum c WHERE c.university = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

    /**
     * Count curricula by specialty
     */
    @Query("SELECT COUNT(c) FROM Curriculum c WHERE c.specialty = :specialtyId")
    long countBySpecialty(@Param("specialtyId") UUID specialtyId);

    /**
     * Check if curriculum with code exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Curriculum c WHERE c.code = :code")
    boolean existsByCode(@Param("code") String code);

    /**
     * Check if curriculum with code exists (excluding current ID)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Curriculum c WHERE c.code = :code AND c.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);
}
