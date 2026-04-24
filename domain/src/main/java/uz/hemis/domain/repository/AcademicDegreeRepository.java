package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.academic.AcademicDegree;

import java.util.Optional;

/**
 * Repository for {@link AcademicDegree} (ilmiy daraja classifier —
 * Фан номзоди, Фан доктори (DSc)).
 *
 * <p>Mapped to legacy {@code hemishe_h_academic_degree}. Read-only lookup by
 * code or localised name — legacy table itself is not modified (rules.md #2).</p>
 *
 * @since 2.1.0
 */
@Repository
@Transactional(readOnly = true)
public interface AcademicDegreeRepository extends JpaRepository<AcademicDegree, String> {

    Optional<AcademicDegree> findByCode(String code);

    /**
     * Case-insensitive name lookup across {@code name} / {@code name_ru}.
     *
     * <p>Used by {@code SacCredentialsSyncService} to map API {@code degree_name}
     * ("Фан номзоди", "Фан доктори (DSc)") to the classifier {@code code}.</p>
     */
    @Query("SELECT d FROM AcademicDegree d " +
           "WHERE (LOWER(d.name) = LOWER(:name) OR LOWER(d.nameRu) = LOWER(:name)) " +
           "AND d.deleteTs IS NULL")
    Optional<AcademicDegree> findByAnyNameIgnoreCase(@Param("name") String name);
}
