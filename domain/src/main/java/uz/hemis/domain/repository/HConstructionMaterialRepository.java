package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.HConstructionMaterial;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface HConstructionMaterialRepository extends JpaRepository<HConstructionMaterial, String> {

    List<HConstructionMaterial> findByIsActiveTrueOrderBySortOrder();
}
