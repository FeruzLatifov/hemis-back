package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.classifier.SpecialityDoctoral;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link SpecialityDoctoral} ({@code hemishe_h_speciality_doctoral}).
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface SpecialityDoctoralRepository extends JpaRepository<SpecialityDoctoral, UUID> {

    List<SpecialityDoctoral> findByActiveTrueOrderByNameAsc();
}
