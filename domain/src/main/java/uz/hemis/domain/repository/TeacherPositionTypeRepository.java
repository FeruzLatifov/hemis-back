package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.TeacherPositionType;

import java.util.List;

/**
 * Repository for TeacherPositionType (Xodim lavozimlari)
 *
 * <p>Primary key: code (String, not UUID)</p>
 * <p>Master/Replica: All read operations routed to Replica</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface TeacherPositionTypeRepository extends JpaRepository<TeacherPositionType, String> {

    List<TeacherPositionType> findByActiveTrue();

    List<TeacherPositionType> findByNameContainingIgnoreCase(String name);
}
