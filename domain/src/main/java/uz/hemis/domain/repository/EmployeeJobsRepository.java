package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.EmployeeJobs;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface EmployeeJobsRepository extends JpaRepository<EmployeeJobs, UUID> {

    @EntityGraph(attributePaths = {"employee"})
    List<EmployeeJobs> findByUniversityCodeAndIsCurrentAndEmployeeTypeCode(String universityCode, boolean isCurrent, String employeeTypeCode);

    @EntityGraph(attributePaths = {"employee"})
    List<EmployeeJobs> findByUniversityCodeAndEmployeeTypeCode(String universityCode, String employeeTypeCode);

    List<EmployeeJobs> findByUniversityCodeAndPositionCodeAndIsCurrent(String universityCode, String positionCode, boolean isCurrent);

    List<EmployeeJobs> findByEmployeeId(UUID employeeId);

    // Legacy compatibility methods — used by EmployeeJobsLegacyService (CUBA API)
    List<EmployeeJobs> findByUniversityCode(String universityCode);

    List<EmployeeJobs> findByEmployeePinfl(String pinfl);

    Page<EmployeeJobs> findByUniversityCode(String universityCode, Pageable pageable);
}
