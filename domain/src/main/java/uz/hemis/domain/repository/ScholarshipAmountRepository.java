package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.ScholarshipAmount;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScholarshipAmountRepository extends JpaRepository<ScholarshipAmount, UUID> {

    List<ScholarshipAmount> findByStudentScholarship(UUID studentScholarshipId);
}
