package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.UniversityAttachedSpeciality;

import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface UniversityAttachedSpecialityRepository extends JpaRepository<UniversityAttachedSpeciality, UUID> {
}
