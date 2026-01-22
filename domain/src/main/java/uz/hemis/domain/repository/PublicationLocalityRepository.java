package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.PublicationLocality;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface PublicationLocalityRepository extends JpaRepository<PublicationLocality, String> {

    List<PublicationLocality> findByActive(Boolean active);

    Page<PublicationLocality> findByActive(Boolean active, Pageable pageable);

    long countByActive(Boolean active);
}
