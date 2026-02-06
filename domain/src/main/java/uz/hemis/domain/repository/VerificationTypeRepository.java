package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.VerificationType;

import java.util.List;

/**
 * Verification Type Repository - DTM verification turlari
 *
 * <p>Table: hemishe_h_verification_type</p>
 * <p>Primary key: code (VARCHAR, not UUID)</p>
 *
 * <p>Soft delete: Entity has @SQLRestriction("delete_ts IS NULL"),
 * so all queries automatically exclude deleted records.</p>
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface VerificationTypeRepository extends JpaRepository<VerificationType, String> {

    List<VerificationType> findByActiveTrue();
}
