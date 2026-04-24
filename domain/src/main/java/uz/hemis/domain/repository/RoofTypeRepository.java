package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.RoofType;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface RoofTypeRepository extends JpaRepository<RoofType, String> {

    List<RoofType> findByIsActiveTrueOrderBySortOrder();
}
