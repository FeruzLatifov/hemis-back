package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.UniversityDepartmentType;

import java.util.List;

/**
 * Repository for {@link UniversityDepartmentType} — OTM bo'linma turlari klassifikatori.
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityDepartmentTypeRepository extends JpaRepository<UniversityDepartmentType, String> {

    List<UniversityDepartmentType> findByActiveTrue();

    List<UniversityDepartmentType> findByNameContainingIgnoreCase(String name);
}
