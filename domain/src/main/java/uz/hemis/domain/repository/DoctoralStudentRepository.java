package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.DoctoralStudent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctoralStudentRepository extends JpaRepository<DoctoralStudent, UUID> {

    Optional<DoctoralStudent> findByDoctoralCode(String doctoralCode);

    Optional<DoctoralStudent> findByStudent(UUID studentId);

    Page<DoctoralStudent> findByUniversity(String universityCode, Pageable pageable);

    List<DoctoralStudent> findByUniversity(String universityCode);

    Page<DoctoralStudent> findByDepartment(UUID departmentId, Pageable pageable);

    List<DoctoralStudent> findByScientificAdvisor(UUID advisorId);

    @Query("SELECT d FROM DoctoralStudent d WHERE d.university = :universityCode AND d.defenseStatus = :status")
    Page<DoctoralStudent> findByUniversityAndStatus(
            @Param("universityCode") String universityCode,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT d FROM DoctoralStudent d WHERE d.university = :universityCode AND d.isActive = true")
    List<DoctoralStudent> findActiveByUniversity(@Param("universityCode") String universityCode);

    @Query("SELECT COUNT(d) FROM DoctoralStudent d WHERE d.university = :universityCode AND d.isActive = true")
    long countActiveByUniversity(@Param("universityCode") String universityCode);

    boolean existsByDoctoralCode(String doctoralCode);

    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
}
