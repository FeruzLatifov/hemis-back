package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.UniversityLifecycle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UniversityLifecycle entity
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>CRUD operations for university lifecycle events</li>
 *   <li>Query events by university code and successor code</li>
 *   <li>Query by event type</li>
 * </ul>
 *
 * <p><strong>Soft Delete Filtering:</strong></p>
 * <ul>
 *   <li>@SQLRestriction("delete_ts IS NULL") on entity</li>
 *   <li>All queries automatically filter deleted records</li>
 * </ul>
 *
 * @see UniversityLifecycle
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityLifecycleRepository extends JpaRepository<UniversityLifecycle, UUID> {

    List<UniversityLifecycle> findByUniversityCodeOrderByEventDateDesc(String universityCode);

    List<UniversityLifecycle> findBySuccessorCodeOrderByEventDateDesc(String successorCode);

    List<UniversityLifecycle> findByEventType(String eventType);

    Optional<UniversityLifecycle> findFirstByUniversityCodeOrderByEventDateDesc(String universityCode);
}
