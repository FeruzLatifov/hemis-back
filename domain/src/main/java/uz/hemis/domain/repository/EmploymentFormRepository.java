package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.EmploymentForm;

import java.util.List;

/**
 * Repository for {@link EmploymentForm} — xodim mehnat shakllari klassifikatori.
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface EmploymentFormRepository extends JpaRepository<EmploymentForm, String> {

    List<EmploymentForm> findByActiveTrue();

    List<EmploymentForm> findByNameContainingIgnoreCase(String name);
}
