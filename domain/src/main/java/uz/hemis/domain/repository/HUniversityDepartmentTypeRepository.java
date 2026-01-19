package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.HUniversityDepartmentType;

import java.util.List;

/**
 * Repository for HUniversityDepartmentType (OTM Bo'linma Turlari)
 *
 * <p>Primary key: code (String, not UUID)</p>
 * <p>Master/Replica: All read operations routed to Replica</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface HUniversityDepartmentTypeRepository extends JpaRepository<HUniversityDepartmentType, String> {

    List<HUniversityDepartmentType> findByActiveTrue();

    List<HUniversityDepartmentType> findByNameContainingIgnoreCase(String name);
}
