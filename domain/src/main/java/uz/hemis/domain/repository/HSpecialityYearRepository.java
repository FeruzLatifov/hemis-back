package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.classifier.HSpecialityYear;

import java.util.List;
import java.util.UUID;

/**
 * Repository for normalized speciality years ({@code h_speciality_year}).
 */
@Repository
@Transactional(readOnly = true)
public interface HSpecialityYearRepository extends JpaRepository<HSpecialityYear, UUID> {

    List<HSpecialityYear> findBySpecialityIdOrderByYearAsc(UUID specialityId);

    /** Years for a batch of specialities — avoids N+1 when rendering a tree/list. */
    @Query("SELECT y FROM HSpecialityYear y WHERE y.specialityId IN :ids ORDER BY y.year ASC")
    List<HSpecialityYear> findBySpecialityIds(@Param("ids") List<UUID> ids);

    /**
     * Valid edition years — the FK target set in {@code h_education_year}. A submitted
     * year must be in this set, else {@code fk_h_speciality_year_year} fails at flush;
     * validating up-front turns an opaque 400 rollback into a clean 422.
     */
    @Query(value = "SELECT ey.year FROM h_education_year ey", nativeQuery = true)
    List<Integer> findValidEducationYears();

    void deleteBySpecialityId(UUID specialityId);
}
