package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.employee.LegacyEmployeeJobs;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for legacy CUBA {@code hemishe_e_employee_jobs} table.
 *
 * <p>api-legacy CRUD endpoint'lari uchun. Yangi {@link EmployeeJobsRepository}
 * (modern {@code employee_job} jadvalga) bilan aralashmaydi.</p>
 *
 * @since 2.6.0
 */
@Repository
public interface LegacyEmployeeJobsRepository extends JpaRepository<LegacyEmployeeJobs, UUID> {

    boolean existsByEmployeeIdAndUniversityAndEmployeePositionAndEmployeeStatus(
            UUID employeeId, String university, String employeePosition, String employeeStatus);

    /**
     * Existing yozuvni unique constraint key bo'yicha topish (idempotent UPSERT uchun).
     * <p>Asosiy 3 ta field bo'yicha qidiradi: employee + university + department.
     * Test data har doim shu uchchasini yuboradi.</p>
     */
    @Query("""
        SELECT e FROM LegacyEmployeeJobs e
        WHERE e.employeeId = :employeeId
          AND e.university = :university
          AND e.department = :department
        ORDER BY e.id DESC
        """)
    java.util.List<LegacyEmployeeJobs> findByEmployeeAndUniversityAndDepartment(
            @Param("employeeId") UUID employeeId,
            @Param("university") String university,
            @Param("department") String department);
}
