package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Employment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, UUID> {

    Optional<Employment> findByEmploymentCode(String employmentCode);

    List<Employment> findByStudent(UUID studentId);

    Page<Employment> findByUniversity(String universityCode, Pageable pageable);

    List<Employment> findByUniversity(String universityCode);

    @Query("SELECT e FROM Employment e WHERE e.university = :universityCode AND e.employmentStatus = :status")
    Page<Employment> findByUniversityAndStatus(
            @Param("universityCode") String universityCode,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT e FROM Employment e WHERE e.student = :studentId AND e.employmentStatus = 'ACTIVE' AND e.isActive = true")
    List<Employment> findActiveByStudent(@Param("studentId") UUID studentId);

    @Query("SELECT e FROM Employment e WHERE e.university = :universityCode AND e.employmentStatus = 'ACTIVE' AND e.isActive = true")
    List<Employment> findActiveByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT COUNT(e) FROM Employment e WHERE e.university = :universityCode AND e.employmentStatus = 'ACTIVE'")
    long countActiveByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT COUNT(e) FROM Employment e WHERE e.university = :universityCode AND e.employmentStatus = 'ACTIVE' AND e.isSpecialtyRelated = true")
    long countSpecialtyRelatedByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT e FROM Employment e WHERE e.university = :universityCode AND e.isSpecialtyRelated = :isRelated")
    Page<Employment> findByUniversityAndSpecialtyRelated(
            @Param("universityCode") String universityCode,
            @Param("isRelated") Boolean isRelated,
            Pageable pageable
    );

    boolean existsByEmploymentCode(String employmentCode);

    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
}
