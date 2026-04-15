package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Employee;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByPinfl(String pinfl);
    boolean existsByPinfl(String pinfl);
}
