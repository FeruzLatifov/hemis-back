package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.academic.AcademicRank;

import java.util.Optional;

/**
 * Repository for {@link AcademicRank} (ilmiy unvon classifier — Доцент, Профессор).
 *
 * <p>Mapped to legacy {@code hemishe_h_academic_rank} (CUBA pattern). Legacy
 * schema may not be altered (rules.md #2), so lookup logic lives here — the
 * read-only queries let the SAC sync service resolve API text ({@code "Доцент"})
 * to a classifier {@code code} without touching the underlying table.</p>
 *
 * @since 2.1.0
 */
@Repository
@Transactional(readOnly = true)
public interface AcademicRankRepository extends JpaRepository<AcademicRank, String> {

    /** Exact-code lookup — fast path when SAC returns a value that already matches our code. */
    Optional<AcademicRank> findByCode(String code);

    /**
     * Case-insensitive name lookup across localised columns ({@code name}, {@code name_ru}).
     *
     * <p>Used by {@code SacCredentialsSyncService} to map API {@code title} ("Доцент") to
     * the classifier {@code code}. Whitespace-trimming is the caller's responsibility.</p>
     *
     * @param name display name in Cyrillic/Uzbek/Russian (any of {@code name} / {@code name_ru})
     * @return classifier row if a single case-insensitive match exists
     */
    @Query("SELECT r FROM AcademicRank r " +
           "WHERE (LOWER(r.name) = LOWER(:name) OR LOWER(r.nameRu) = LOWER(:name)) " +
           "AND r.deleteTs IS NULL")
    Optional<AcademicRank> findByAnyNameIgnoreCase(@Param("name") String name);
}
