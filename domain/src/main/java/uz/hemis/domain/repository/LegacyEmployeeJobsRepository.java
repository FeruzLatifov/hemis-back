package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.legacy.employee.LegacyEmployeeJobs;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for legacy CUBA {@code hemishe_e_employee_jobs} table.
 *
 * <p>api-legacy CRUD endpoint'lari (Univer Yii2 PHP integratsiya) uchun.
 * Yangi {@code EmployeeJobsRepository} (modern {@code employee_job} jadval) bilan
 * aralashmaydi — api-legacy/CLAUDE.md GOLDEN RULE.</p>
 *
 * @since 2.6.0
 */
@Repository
@Transactional(readOnly = true)
public interface LegacyEmployeeJobsRepository extends JpaRepository<LegacyEmployeeJobs, UUID> {

    boolean existsByEmployeeIdAndUniversityAndEmployeePositionAndEmployeeStatus(
            UUID employeeId, String university, String employeePosition, String employeeStatus);

    /**
     * Existing yozuvni unique constraint key bo'yicha topish (idempotent UPSERT uchun).
     * <p>Asosiy 3 ta field bo'yicha qidiradi: employee + university + department.</p>
     */
    @Query("""
        SELECT e FROM LegacyEmployeeJobs e
        WHERE e.employeeId = :employeeId
          AND e.university = :university
          AND e.department = :department
        ORDER BY e.id DESC
        """)
    List<LegacyEmployeeJobs> findByEmployeeAndUniversityAndDepartment(
            @Param("employeeId") UUID employeeId,
            @Param("university") String university,
            @Param("department") String department);

    /** Univer cross-tenant search uchun universitet kodi bo'yicha. */
    List<LegacyEmployeeJobs> findByUniversity(String university);

    /** Pagination bilan universitet bo'yicha (CUBA pagination). */
    Page<LegacyEmployeeJobs> findByUniversity(String university, Pageable pageable);

    /** Xodim UUID si bo'yicha barcha lavozimlar. */
    List<LegacyEmployeeJobs> findByEmployeeId(UUID employeeId);

    /**
     * Teacher.code (PINFL) bo'yicha lavozimlarni topish.
     * <p>{@code hemishe_e_teacher.code} — xodimning PINFL'i (CUBA legacy).</p>
     */
    @Query(value = """
            SELECT ej.* FROM hemishe_e_employee_jobs ej
            JOIN hemishe_e_teacher t ON ej._employee = t.id
            WHERE t.code = :pinfl
              AND ej.delete_ts IS NULL
              AND t.delete_ts IS NULL
            """,
           nativeQuery = true)
    List<LegacyEmployeeJobs> findByEmployeePinfl(@Param("pinfl") String pinfl);
}
