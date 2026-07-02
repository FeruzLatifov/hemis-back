package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.classifier.SpecialityBachelor;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link SpecialityBachelor} ({@code hemishe_h_speciality_bachelor}).
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface SpecialityBachelorRepository extends JpaRepository<SpecialityBachelor, UUID> {

    List<SpecialityBachelor> findByActiveTrueOrderByNameAsc();
}
