package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.PublicationMethodical;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PublicationMethodicalRepository extends JpaRepository<PublicationMethodical, UUID> {

    List<PublicationMethodical> findByUniversity(UUID university);

    List<PublicationMethodical> findByUniversityAndEducationYear(UUID university, UUID educationYear);

    Page<PublicationMethodical> findByUniversityAndEducationYear(UUID university, UUID educationYear, Pageable pageable);

    long countByUniversityAndEducationYear(UUID university, UUID educationYear);
}
