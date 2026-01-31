package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.HCourse;

@Repository
@Transactional(readOnly = true)
public interface HCourseRepository extends JpaRepository<HCourse, String> {
}
