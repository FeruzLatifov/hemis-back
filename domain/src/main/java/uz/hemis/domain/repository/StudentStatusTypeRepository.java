package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.student.StudentStatusType;

import java.util.List;

/**
 * Repository for StudentStatusType (Talaba holatlari)
 *
 * <p>Primary key: code (String, not UUID)</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface StudentStatusTypeRepository extends JpaRepository<StudentStatusType, String> {

    List<StudentStatusType> findByActiveTrue();

    List<StudentStatusType> findByNameContainingIgnoreCase(String name);
}
