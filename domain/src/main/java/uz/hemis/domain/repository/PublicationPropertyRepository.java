package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.PublicationProperty;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PublicationPropertyRepository extends JpaRepository<PublicationProperty, UUID> {

    List<PublicationProperty> findByUniversity(UUID university);

    List<PublicationProperty> findByUniversityAndEducationYear(UUID university, UUID educationYear);

    Page<PublicationProperty> findByUniversityAndEducationYear(UUID university, UUID educationYear, Pageable pageable);

    long countByUniversityAndEducationYear(UUID university, UUID educationYear);
}
