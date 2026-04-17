package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.EmployeeJobs;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface EmployeeJobsRepository extends JpaRepository<EmployeeJobs, UUID> {

    @EntityGraph(attributePaths = {"employee"})
    List<EmployeeJobs> findByUniversityCodeAndIsCurrentAndEmployeeType(String universityCode, boolean isCurrent, String employeeType);

    @EntityGraph(attributePaths = {"employee"})
    List<EmployeeJobs> findByUniversityCodeAndEmployeeType(String universityCode, String employeeType);

    List<EmployeeJobs> findByUniversityCodeAndPositionCodeAndIsCurrent(String universityCode, String positionCode, boolean isCurrent);

    List<EmployeeJobs> findByEmployeeId(UUID employeeId);

    // Legacy compatibility methods — used by EmployeeJobsLegacyService (CUBA API)
    List<EmployeeJobs> findByUniversityCode(String universityCode);

    List<EmployeeJobs> findByEmployeePinfl(String pinfl);

    Page<EmployeeJobs> findByUniversityCode(String universityCode, Pageable pageable);
}
