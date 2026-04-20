package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.employee.UniversityEmployeeStatusType;

import java.util.List;

/**
 * Repository for UniversityEmployeeStatusType (Xodim holatlari)
 *
 * <p>Primary key: code (String, not UUID)</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityEmployeeStatusTypeRepository extends JpaRepository<UniversityEmployeeStatusType, String> {

    /**
     * Faol holatlarni olish
     */
    List<UniversityEmployeeStatusType> findByActiveTrue();

    /**
     * Nom bo'yicha qidirish
     */
    List<UniversityEmployeeStatusType> findByNameContainingIgnoreCase(String name);
}
