package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.Verification;

import java.util.List;
import java.util.UUID;

/**
 * Verification Repository - DTM verification ballari
 *
 * <p>Table: hemishe_e_verification</p>
 *
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface VerificationRepository extends JpaRepository<Verification, UUID> {

    /**
     * Find all verifications by PINFL
     *
     * <p>OLD-HEMIS compatible - returns all verification records for a given PINFL</p>
     *
     * @param pinfl Personal identification number
     * @return List of verification records
     */
    List<Verification> findByPinfl(String pinfl);

    /**
     * Count verifications by PINFL
     *
     * @param pinfl Personal identification number
     * @return Count of verification records
     */
    long countByPinfl(String pinfl);

    /**
     * Check if verification exists for PINFL
     *
     * @param pinfl Personal identification number
     * @return true if verification exists
     */
    boolean existsByPinfl(String pinfl);

    /**
     * Find verifications by university code
     *
     * @param university University code
     * @return List of verification records
     */
    List<Verification> findByUniversity(String university);

    /**
     * Find verifications by PINFL and education year
     *
     * @param pinfl Personal identification number
     * @param educationYear Education year code
     * @return List of verification records
     */
    List<Verification> findByPinflAndEducationYear(String pinfl, String educationYear);
}
