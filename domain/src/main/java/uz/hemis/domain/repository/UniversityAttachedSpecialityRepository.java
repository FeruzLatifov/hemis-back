package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.UniversityAttachedSpeciality;

import java.util.UUID;

/**
 * Repository for {@link UniversityAttachedSpeciality}
 * ({@code hemishe_e_university_attached_speciality}).
 *
 * <p>The entity carries {@code @SQLRestriction("delete_ts IS NULL")}, so every JPQL
 * query below is automatically scoped to non-deleted rows. The duplicate-guard methods
 * check whether an <em>active</em> attachment with the same
 * (university, educationType, educationForm, speciality-column) already exists,
 * optionally excluding the row being updated ({@code excludeId}). {@code educationForm}
 * is nullable, so a NULL-safe comparison is used.</p>
 *
 * @since 2.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface UniversityAttachedSpecialityRepository extends JpaRepository<UniversityAttachedSpeciality, UUID> {

    @Query("""
            SELECT COUNT(e) > 0 FROM UniversityAttachedSpeciality e
            WHERE e.university = :university
              AND e.educationType = :educationType
              AND ((:educationForm IS NULL AND e.educationForm IS NULL) OR e.educationForm = :educationForm)
              AND e.specialityBachelor = :specialityId
              AND (:excludeId IS NULL OR e.id <> :excludeId)
            """)
    boolean existsBachelorDuplicate(@Param("university") String university,
                                    @Param("educationType") String educationType,
                                    @Param("educationForm") String educationForm,
                                    @Param("specialityId") UUID specialityId,
                                    @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT COUNT(e) > 0 FROM UniversityAttachedSpeciality e
            WHERE e.university = :university
              AND e.educationType = :educationType
              AND ((:educationForm IS NULL AND e.educationForm IS NULL) OR e.educationForm = :educationForm)
              AND e.specialityMaster = :specialityId
              AND (:excludeId IS NULL OR e.id <> :excludeId)
            """)
    boolean existsMasterDuplicate(@Param("university") String university,
                                  @Param("educationType") String educationType,
                                  @Param("educationForm") String educationForm,
                                  @Param("specialityId") UUID specialityId,
                                  @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT COUNT(e) > 0 FROM UniversityAttachedSpeciality e
            WHERE e.university = :university
              AND e.educationType = :educationType
              AND ((:educationForm IS NULL AND e.educationForm IS NULL) OR e.educationForm = :educationForm)
              AND e.specialityOrdinatura = :specialityId
              AND (:excludeId IS NULL OR e.id <> :excludeId)
            """)
    boolean existsOrdinaturaDuplicate(@Param("university") String university,
                                      @Param("educationType") String educationType,
                                      @Param("educationForm") String educationForm,
                                      @Param("specialityId") UUID specialityId,
                                      @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT COUNT(e) > 0 FROM UniversityAttachedSpeciality e
            WHERE e.university = :university
              AND e.educationType = :educationType
              AND ((:educationForm IS NULL AND e.educationForm IS NULL) OR e.educationForm = :educationForm)
              AND e.specialityDoctoral = :specialityId
              AND (:excludeId IS NULL OR e.id <> :excludeId)
            """)
    boolean existsDoctoralDuplicate(@Param("university") String university,
                                    @Param("educationType") String educationType,
                                    @Param("educationForm") String educationForm,
                                    @Param("specialityId") UUID specialityId,
                                    @Param("excludeId") UUID excludeId);
}
