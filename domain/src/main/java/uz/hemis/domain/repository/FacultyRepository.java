package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Faculty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Faculty entity
 *
 * <p><strong>NO-DELETE Constraint (NDG):</strong></p>
 * <ul>
 *   <li>NO delete() methods defined</li>
 *   <li>NO deleteById() methods defined</li>
 *   <li>NO deleteAll() methods defined</li>
 *   <li>Soft delete ONLY (set deleteTs in service layer)</li>
 * </ul>
 *
 * @see Faculty
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {

    // NO DELETE METHODS (NDG)

    /**
     * Find faculty by code
     */
    Optional<Faculty> findByCode(String code);

    /**
     * Find faculties by university code
     */
    @Query("SELECT f FROM Faculty f WHERE f.university = :universityCode")
    Page<Faculty> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    /**
     * Find all faculties by university (no pagination)
     */
    @Query("SELECT f FROM Faculty f WHERE f.university = :universityCode")
    List<Faculty> findAllByUniversity(@Param("universityCode") String universityCode);

    /**
     * Find faculties by name (partial match, case-insensitive)
     */
    @Query("SELECT f FROM Faculty f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Faculty> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find active faculties
     */
    @Query("SELECT f FROM Faculty f WHERE f.active = true")
    Page<Faculty> findByActiveTrue(Pageable pageable);

    /**
     * Find faculties by type
     */
    @Query("SELECT f FROM Faculty f WHERE f.facultyType = :typeCode")
    Page<Faculty> findByFacultyType(@Param("typeCode") String typeCode, Pageable pageable);

    /**
     * Count faculties by university
     */
    @Query("SELECT COUNT(f) FROM Faculty f WHERE f.university = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

    /**
     * Check if faculty with code exists
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Faculty f WHERE f.code = :code")
    boolean existsByCode(@Param("code") String code);

    /**
     * Check if faculty with code exists (excluding current ID)
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Faculty f WHERE f.code = :code AND f.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);
}
