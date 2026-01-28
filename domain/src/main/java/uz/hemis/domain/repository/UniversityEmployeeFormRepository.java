package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.UniversityEmployeeForm;

import java.util.List;

/**
 * Repository for UniversityEmployeeForm (Xodim mehnat shakllari)
 *
 * <p>Primary key: code (String, not UUID)</p>
 *
 * <p>Mehnat shakllari:</p>
 * <ul>
 *   <li>11 - Asosiy shtat</li>
 *   <li>12 - Ichki o'rindoshlik</li>
 *   <li>13 - Tashqi o'rindoshlik</li>
 *   <li>14 - Soatbay</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityEmployeeFormRepository extends JpaRepository<UniversityEmployeeForm, String> {

    /**
     * Faol shakllarni olish
     */
    List<UniversityEmployeeForm> findByActiveTrue();

    /**
     * Nom bo'yicha qidirish
     */
    List<UniversityEmployeeForm> findByNameContainingIgnoreCase(String name);
}
