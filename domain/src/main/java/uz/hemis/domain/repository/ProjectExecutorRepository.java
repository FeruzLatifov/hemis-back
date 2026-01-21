package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.ProjectExecutor;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface ProjectExecutorRepository extends JpaRepository<ProjectExecutor, UUID> {

    List<ProjectExecutor> findByProject(UUID project);

    List<ProjectExecutor> findByProjectAndProjectExecutorType(UUID project, String projectExecutorType);

    Page<ProjectExecutor> findByProjectAndProjectExecutorType(UUID project, String projectExecutorType, Pageable pageable);

    long countByProjectAndProjectExecutorType(UUID project, String projectExecutorType);
}
