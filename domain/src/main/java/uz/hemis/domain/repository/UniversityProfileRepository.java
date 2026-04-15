package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.UniversityProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UniversityProfile — university public profile (contacts, social, documents).
 *
 * <p>1:1 with hemishe_e_university via {@code university_code}.</p>
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityProfileRepository extends JpaRepository<UniversityProfile, UUID> {

    Optional<UniversityProfile> findByUniversityCode(String universityCode);

    boolean existsByUniversityCode(String universityCode);
}
