package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.finance.ScholarshipAmount;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface ScholarshipAmountRepository extends JpaRepository<ScholarshipAmount, UUID> {

    List<ScholarshipAmount> findByStudentScholarship(UUID studentScholarshipId);
}
