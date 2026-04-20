package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.student.DoctoralStudentType;

import java.util.List;

/**
 * Repository for {@link DoctoralStudentType} — doktorantura talabasi turi klassifikatori.
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface DoctoralStudentTypeRepository extends JpaRepository<DoctoralStudentType, String> {

    List<DoctoralStudentType> findByActiveTrue();
}
