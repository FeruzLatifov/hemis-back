package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.classifier.SpecialityMaster;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link SpecialityMaster} ({@code hemishe_h_speciality_master}).
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface SpecialityMasterRepository extends JpaRepository<SpecialityMaster, UUID> {

    List<SpecialityMaster> findByActiveTrueOrderByNameAsc();
}
