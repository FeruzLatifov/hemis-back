package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.academic.Specialty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Specialty entity (hemishe_e_university_speciality)
 *
 * @see Specialty
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    Optional<Specialty> findByCode(String code);

    @Query("SELECT s FROM Specialty s WHERE s.university = :universityCode")
    Page<Specialty> findByUniversity(@Param("universityCode") String universityCode, Pageable pageable);

    @Query("SELECT s FROM Specialty s WHERE s.faculty = :facultyCode")
    Page<Specialty> findByFaculty(@Param("facultyCode") String facultyCode, Pageable pageable);

    @Query("SELECT s FROM Specialty s WHERE s.faculty = :facultyCode")
    List<Specialty> findAllByFaculty(@Param("facultyCode") String facultyCode);

    @Query("SELECT s FROM Specialty s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Specialty> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT s FROM Specialty s WHERE s.active = true")
    Page<Specialty> findByActiveTrue(Pageable pageable);

    @Query("SELECT s FROM Specialty s WHERE s.educationType = :typeCode")
    Page<Specialty> findByEducationType(@Param("typeCode") String typeCode, Pageable pageable);

    @Query("SELECT s FROM Specialty s WHERE s.university = :universityCode AND s.educationType = :typeCode")
    Page<Specialty> findByUniversityAndEducationType(
            @Param("universityCode") String universityCode,
            @Param("typeCode") String typeCode,
            Pageable pageable
    );

    @Query("SELECT COUNT(s) FROM Specialty s WHERE s.university = :universityCode")
    long countByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT COUNT(s) FROM Specialty s WHERE s.faculty = :facultyCode")
    long countByFaculty(@Param("facultyCode") String facultyCode);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Specialty s WHERE s.code = :code")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Specialty s WHERE s.code = :code AND s.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);

    @Query("SELECT s FROM Specialty s WHERE s.university = :university AND s.educationType = :educationType AND s.educationYear = :educationYear AND s.code = :code AND s.name = :name")
    Optional<Specialty> findByUniqueKey(
            @Param("university") String university,
            @Param("educationType") String educationType,
            @Param("educationYear") String educationYear,
            @Param("code") String code,
            @Param("name") String name
    );
}
