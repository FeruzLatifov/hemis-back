package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.EmployeeJobs;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface EmployeeJobsRepository extends JpaRepository<EmployeeJobs, UUID>, EmployeeJobUpsertRepository {

    @EntityGraph(attributePaths = {"employee"})
    List<EmployeeJobs> findByUniversityCodeAndIsCurrentAndPositionTypeCode(String universityCode, boolean isCurrent, String positionTypeCode);

    @EntityGraph(attributePaths = {"employee"})
    List<EmployeeJobs> findByUniversityCodeAndPositionTypeCode(String universityCode, String positionTypeCode);

    List<EmployeeJobs> findByUniversityCodeAndPositionCodeAndIsCurrent(String universityCode, String positionCode, boolean isCurrent);

    List<EmployeeJobs> findByEmployeeId(UUID employeeId);

    // Legacy compatibility methods — used by EmployeeJobsLegacyService (CUBA API)
    List<EmployeeJobs> findByUniversityCode(String universityCode);

    /**
     * Employee.pinfl — Pinfl VO (PinflConverter bilan). Spring Data derivation
     * VO type bilan ishonchsiz — explicit native query xavfsizroq.
     */
    @Query(value = "SELECT ej.* FROM employee_job ej " +
                   "JOIN employee e ON ej.employee_id = e.id " +
                   "WHERE e.pinfl = :pinfl AND ej.deleted_at IS NULL",
           nativeQuery = true)
    List<EmployeeJobs> findByEmployeePinfl(@Param("pinfl") String pinfl);

    Page<EmployeeJobs> findByUniversityCode(String universityCode, Pageable pageable);

    /**
     * Concurrent addJob race detection — boolean exists (audit P1.T4).
     *
     * <p>Used by {@code TeacherCubaService.addJob} pre-check: if employee already has
     * a current (active, soft-not-deleted) job at the same university+position,
     * reject the new request before save.</p>
     */
    boolean existsByEmployeeIdAndUniversityCodeAndPositionCodeAndIsCurrentAndDeletedAtIsNull(
            UUID employeeId,
            String universityCode,
            String positionCode,
            boolean isCurrent);

    /**
     * Convenience overload — pre-check assumes isCurrent=true.
     */
    default boolean existsByEmployeeIdAndUniversityCodeAndPositionCodeAndIsCurrentAndDeletedAtIsNull(
            UUID employeeId, String universityCode, String positionCode) {
        return existsByEmployeeIdAndUniversityCodeAndPositionCodeAndIsCurrentAndDeletedAtIsNull(
                employeeId, universityCode, positionCode, true);
    }

    /**
     * Univer sync upsert key — (university_code, source_uid) (V015 unique partial index).
     * source_uid Univer'ning ichki ID, idempotent lookup uchun.
     */
    @Query(value = "SELECT * FROM employee_job " +
                   "WHERE university_code = :universityCode AND source_uid = :sourceUid " +
                   "AND deleted_at IS NULL LIMIT 1",
           nativeQuery = true)
    java.util.Optional<EmployeeJobs> findByUniversityCodeAndSourceUid(
            @Param("universityCode") String universityCode,
            @Param("sourceUid") String sourceUid);
}
