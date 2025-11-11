package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.ProjectMeta;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface ProjectMetaRepository extends JpaRepository<ProjectMeta, UUID> {

    List<ProjectMeta> findByProject(UUID project);

    List<ProjectMeta> findByProjectAndFiscalYear(UUID project, Integer fiscalYear);

    Page<ProjectMeta> findByProjectAndFiscalYear(UUID project, Integer fiscalYear, Pageable pageable);

    long countByProjectAndFiscalYear(UUID project, Integer fiscalYear);
}
