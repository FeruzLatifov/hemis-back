package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.finance.DiplomaBlankDistribution;

import java.util.UUID;

/**
 * Repository for {@link DiplomaBlankDistribution}.
 *
 * <p>{@code @SQLRestriction("deleted_at IS NULL")} on the entity keeps all
 * JPA reads/lookups scoped to non-deleted rows automatically.</p>
 */
@Repository
@Transactional(readOnly = true)
public interface DiplomaBlankDistributionRepository
        extends JpaRepository<DiplomaBlankDistribution, UUID>,
                JpaSpecificationExecutor<DiplomaBlankDistribution> {
}
