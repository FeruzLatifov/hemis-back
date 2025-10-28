package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Department (Cathedra) Repository
 *
 * CRITICAL - NO DELETE OPERATIONS:
 * - Physical DELETE is prohibited (NDG - Non-Deletion Guarantee)
 * - Use soft delete (update delete_ts) in service layer
 *
 * Legacy API: /app/rest/cathedra/get
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    // =====================================================
    // Query Methods (Spring Data JPA)
    // =====================================================

    Optional<Department> findByDepartmentCode(String departmentCode);

    Page<Department> findByUniversity(String universityCode, Pageable pageable);

    List<Department> findByUniversity(String universityCode);

    Page<Department> findByFaculty(UUID facultyId, Pageable pageable);

    List<Department> findByFaculty(UUID facultyId);

    @Query("SELECT d FROM Department d WHERE d.university = :universityCode AND d.isActive = true")
    List<Department> findActiveByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT d FROM Department d WHERE d.faculty = :facultyId AND d.isActive = true")
    List<Department> findActiveByFaculty(@Param("facultyId") UUID facultyId);

    @Query("SELECT d FROM Department d WHERE d.head = :headId")
    Optional<Department> findByHead(@Param("headId") UUID headId);

    @Query("SELECT d FROM Department d WHERE d.university = :universityCode AND d.departmentType = :type")
    List<Department> findByUniversityAndType(
            @Param("universityCode") String universityCode,
            @Param("type") String type
    );

    @Query("SELECT COUNT(d) FROM Department d WHERE d.university = :universityCode AND d.isActive = true")
    long countActiveByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT COUNT(d) FROM Department d WHERE d.faculty = :facultyId AND d.isActive = true")
    long countActiveByFaculty(@Param("facultyId") UUID facultyId);

    boolean existsByDepartmentCode(String departmentCode);

    // =====================================================
    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
    // =====================================================
    // void deleteById(UUID id);           ← PROHIBITED
    // void delete(Department entity);     ← PROHIBITED
    // void deleteAll();                   ← PROHIBITED
    //
    // Use soft delete in service layer:
    // department.setDeleteTs(LocalDateTime.now());
    // departmentRepository.save(department);
    // =====================================================
}
