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
public interface EmployeeJobsRepository extends JpaRepository<EmployeeJobs, UUID> {

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
}
