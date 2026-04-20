package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.research.PublicationAuthorMeta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PublicationAuthorMetaRepository extends JpaRepository<PublicationAuthorMeta, UUID> {

    List<PublicationAuthorMeta> findByUniversity(String university);

    Page<PublicationAuthorMeta> findByUniversity(String university, Pageable pageable);

    List<PublicationAuthorMeta> findByUniversityAndEmployee(String university, UUID employee);

    Page<PublicationAuthorMeta> findByUniversityAndEmployee(String university, UUID employee, Pageable pageable);

    long countByUniversity(String university);

    long countByUniversityAndEmployee(String university, UUID employee);

    Optional<PublicationAuthorMeta> findByIdAndUniversity(UUID id, String university);
}
