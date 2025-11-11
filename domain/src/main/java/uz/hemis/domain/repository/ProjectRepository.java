package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Project;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByUniversity(UUID university);

    List<Project> findByUniversityAndDepartment(UUID university, UUID department);

    Page<Project> findByUniversityAndDepartment(UUID university, UUID department, Pageable pageable);

    long countByUniversityAndDepartment(UUID university, UUID department);
}
