package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.EmployeeRate;

import java.util.List;

/**
 * Repository for {@link EmployeeRate} — xodim ish stavkasi klassifikatori.
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface EmployeeRateRepository extends JpaRepository<EmployeeRate, String> {

    List<EmployeeRate> findByActiveTrue();

    List<EmployeeRate> findByNameContainingIgnoreCase(String name);
}
