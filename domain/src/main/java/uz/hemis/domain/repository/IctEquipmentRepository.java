package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.IctEquipment;

import java.util.List;
import java.util.UUID;

/**
 * IctEquipment Repository
 *
 * PHASE 5: Infrastructure
 */
@Repository
@Transactional(readOnly = true)
public interface IctEquipmentRepository extends JpaRepository<IctEquipment, UUID> {

    List<IctEquipment> findByUniversity(String university);

    List<IctEquipment> findByUniversityAndEducationYear(String university, String educationYear);

    Page<IctEquipment> findByUniversityAndEducationYear(String university, String educationYear, Pageable pageable);

    long countByUniversityAndEducationYear(String university, String educationYear);
}
