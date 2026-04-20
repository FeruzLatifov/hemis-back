package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.Citizenship;

import java.util.List;

/**
 * Repository for Citizenship (Fuqarolik holatlari)
 *
 * <p>Primary key: code (String, not UUID)</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface CitizenshipRepository extends JpaRepository<Citizenship, String> {

    List<Citizenship> findByActiveTrue();

    List<Citizenship> findByNameContainingIgnoreCase(String name);
}
