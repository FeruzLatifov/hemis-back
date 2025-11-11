package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.PublicationAuthorMeta;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PublicationAuthorMetaRepository extends JpaRepository<PublicationAuthorMeta, UUID> {

    List<PublicationAuthorMeta> findByUniversity(UUID university);

    List<PublicationAuthorMeta> findByUniversityAndEmployee(UUID university, UUID employee);

    Page<PublicationAuthorMeta> findByUniversityAndEmployee(UUID university, UUID employee, Pageable pageable);

    long countByUniversityAndEmployee(UUID university, UUID employee);
}
