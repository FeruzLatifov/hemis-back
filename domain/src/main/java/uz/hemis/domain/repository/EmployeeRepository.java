package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.vo.Pinfl;
import uz.hemis.domain.entity.employee.Employee;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByPinfl(Pinfl pinfl);
    boolean existsByPinfl(Pinfl pinfl);
}
