package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.UniversityEmployeeType;

import java.util.List;

/**
 * Repository for UniversityEmployeeType (Xodim turlari)
 *
 * <p>Primary key: code (String, not UUID)</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityEmployeeTypeRepository extends JpaRepository<UniversityEmployeeType, String> {

    List<UniversityEmployeeType> findByActiveTrue();

    List<UniversityEmployeeType> findByNameContainingIgnoreCase(String name);
}
