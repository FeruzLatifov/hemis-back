package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.student.Expel;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface ExpelRepository extends JpaRepository<Expel, UUID> {

    List<Expel> findByUniversityCode(String universityCode);

    Page<Expel> findByUniversityCode(String universityCode, Pageable pageable);

    List<Expel> findByEducationYearCode(String educationYearCode);

    List<Expel> findByExpelReasonCode(String expelReasonCode);
}
