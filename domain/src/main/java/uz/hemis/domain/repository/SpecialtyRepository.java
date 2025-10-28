package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Specialty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Specialty entity
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong></p>
 * <ul>
 *   <li>NO delete() methods defined</li>
 *   <li>NO deleteById() methods defined</li>
 *   <li>NO deleteAll() methods defined</li>
 *   <li>Soft delete ONLY (set deleteTs in service layer)</li>
 * </ul>
 *
 * @see Specialty
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    // NO DELETE METHODS (NDG)

    /**
     * Find specialty by code
     */
    Optional<Specialty> findByCode(String code);

    /**
     * Find specialties by university code
     */
    @Query("SELECT s FROM Specialty s WHERE s.university = :universityCode")
    Page<Specialty> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    /**
     * Find specialties by faculty ID
     */
    @Query("SELECT s FROM Specialty s WHERE s.faculty = :facultyId")
    Page<Specialty> findByFaculty(@Param("facultyId") UUID facultyId, Pageable pageable);

    /**
     * Find all specialties by faculty (no pagination)
     */
    @Query("SELECT s FROM Specialty s WHERE s.faculty = :facultyId")
    List<Specialty> findAllByFaculty(@Param("facultyId") UUID facultyId);

    /**
     * Find specialties by name (partial match, case-insensitive)
     */
    @Query("SELECT s FROM Specialty s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Specialty> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find active specialties
     */
    @Query("SELECT s FROM Specialty s WHERE s.active = true")
    Page<Specialty> findByActiveTrue(Pageable pageable);

    /**
     * Find specialties by education type
     */
    @Query("SELECT s FROM Specialty s WHERE s.educationType = :typeCode")
    Page<Specialty> findByEducationType(@Param("typeCode") String typeCode, Pageable pageable);

    /**
     * Find specialties by education form
     */
    @Query("SELECT s FROM Specialty s WHERE s.educationForm = :formCode")
    Page<Specialty> findByEducationForm(@Param("formCode") String formCode, Pageable pageable);

    /**
     * Find specialties by university and education type
     */
    @Query("SELECT s FROM Specialty s WHERE s.university = :universityCode AND s.educationType = :typeCode")
    Page<Specialty> findByUniversityAndEducationType(
            @Param("universityCode") String universityCode,
            @Param("typeCode") String typeCode,
            Pageable pageable
    );

    /**
     * Count specialties by university
     */
    @Query("SELECT COUNT(s) FROM Specialty s WHERE s.university = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

    /**
     * Count specialties by faculty
     */
    @Query("SELECT COUNT(s) FROM Specialty s WHERE s.faculty = :facultyId")
    long countByFaculty(@Param("facultyId") UUID facultyId);

    /**
     * Check if specialty with code exists
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Specialty s WHERE s.code = :code")
    boolean existsByCode(@Param("code") String code);

    /**
     * Check if specialty with code exists (excluding current ID)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Specialty s WHERE s.code = :code AND s.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);
}
