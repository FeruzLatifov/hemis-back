package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.research.PublicationMethodical;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PublicationMethodicalRepository extends JpaRepository<PublicationMethodical, UUID> {

    List<PublicationMethodical> findByUniversity(String university);

    List<PublicationMethodical> findByUniversityAndEducationYear(String university, String educationYear);

    Page<PublicationMethodical> findByUniversityAndEducationYear(String university, String educationYear, Pageable pageable);

    long countByUniversityAndEducationYear(String university, String educationYear);
}
