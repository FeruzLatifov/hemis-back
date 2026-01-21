package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Project;

import java.util.List;
import java.util.UUID;

/**
 * Project Repository
 *
 * Note: university and department fields are String (classifier codes),
 * not UUID. This matches the legacy database schema.
 */
@Repository
@Transactional(readOnly = true)
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByUniversity(String university);

    List<Project> findByUniversityAndDepartment(String university, String department);

    Page<Project> findByUniversityAndDepartment(String university, String department, Pageable pageable);

    long countByUniversityAndDepartment(String university, String department);
}
