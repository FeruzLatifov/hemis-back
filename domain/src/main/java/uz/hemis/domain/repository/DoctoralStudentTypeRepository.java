package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.DoctoralStudentType;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface DoctoralStudentTypeRepository extends JpaRepository<DoctoralStudentType, String> {

    List<DoctoralStudentType> findByActive(Boolean active);

    Page<DoctoralStudentType> findByActive(Boolean active, Pageable pageable);

    long countByActive(Boolean active);
}
