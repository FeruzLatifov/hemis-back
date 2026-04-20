package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.student.DoctoralStudentStatus;

import java.util.List;

/**
 * Repository for {@link DoctoralStudentStatus} — doktorantura talabasi statusi klassifikatori.
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface DoctoralStudentStatusRepository extends JpaRepository<DoctoralStudentStatus, String> {

    List<DoctoralStudentStatus> findByActiveTrue();
}
