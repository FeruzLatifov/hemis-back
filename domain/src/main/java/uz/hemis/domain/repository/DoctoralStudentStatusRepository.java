package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.DoctoralStudentStatus;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface DoctoralStudentStatusRepository extends JpaRepository<DoctoralStudentStatus, String> {

    List<DoctoralStudentStatus> findByActive(Boolean active);

    Page<DoctoralStudentStatus> findByActive(Boolean active, Pageable pageable);

    long countByActive(Boolean active);
}
