package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.research.PublicationLocality;

import java.util.List;

/**
 * Repository for {@link PublicationLocality} — nashr hududi klassifikatori.
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface PublicationLocalityRepository extends JpaRepository<PublicationLocality, String> {

    List<PublicationLocality> findByActiveTrue();
}
