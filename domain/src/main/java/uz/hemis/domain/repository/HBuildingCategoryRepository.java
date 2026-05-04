package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.infrastructure.HBuildingCategory;

import java.util.List;

/**
 * Repository for {@link HBuildingCategory} classifier.
 * 6 boshlang'ich qiymat (ACADEMIC, DORMITORY, ...) kengayuvchi.
 */
@Repository
@Transactional(readOnly = true)
public interface HBuildingCategoryRepository extends JpaRepository<HBuildingCategory, String> {

    List<HBuildingCategory> findByIsActiveTrueOrderBySortOrder();
}
