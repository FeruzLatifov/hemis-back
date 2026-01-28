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

/**
 * Repository for DoctoralStudent entity
 *
 * Table: hemishe_e_doctorate_student
 * All methods match actual entity fields from ministry.sql
 *
 * NO-DELETE • NO-RENAME • NO-BREAKING-CHANGES
 */
@Repository
public interface DoctoralStudentRepository extends JpaRepository<DoctoralStudent, UUID> {

    /**
     * Find by passport PIN (PINFL)
     */
    Optional<DoctoralStudent> findByPassportPin(String passportPin);

    /**
     * Find by passport number
     */
    Optional<DoctoralStudent> findByPassportNumber(String passportNumber);

    /**
     * Find by student ID number
     */
    Optional<DoctoralStudent> findByStudentIdNumber(String studentIdNumber);

    /**
     * Find by legacy U_ID
     */
    @Query("SELECT d FROM DoctoralStudent d WHERE d.uId = :uId")
    Optional<DoctoralStudent> findByLegacyUId(@Param("uId") Integer uId);

    /**
     * Find by university code with pagination
     */
    Page<DoctoralStudent> findByUniversity(String university, Pageable pageable);

    /**
     * Find all by university code
     */
    List<DoctoralStudent> findByUniversity(String university);

    /**
     * Find by department code with pagination
     */
    Page<DoctoralStudent> findByDepartment(String department, Pageable pageable);

    /**
     * Find by doctoral student type
     */
    List<DoctoralStudent> findByDoctoralStudentType(String doctoralStudentType);

    /**
     * Find by doctorate student status
     */
    List<DoctoralStudent> findByDoctorateStudentStatus(String doctorateStudentStatus);

    /**
     * Find active doctoral students by university
     */
    @Query("SELECT d FROM DoctoralStudent d WHERE d.university = :university AND d.active = true")
    List<DoctoralStudent> findActiveByUniversity(@Param("university") String university);

    /**
     * Count active doctoral students by university
     */
    @Query("SELECT COUNT(d) FROM DoctoralStudent d WHERE d.university = :university AND d.active = true")
    long countActiveByUniversity(@Param("university") String university);

    /**
     * Find by university and status
     */
    @Query("SELECT d FROM DoctoralStudent d WHERE d.university = :university AND d.doctorateStudentStatus = :status")
    Page<DoctoralStudent> findByUniversityAndStatus(
            @Param("university") String university,
            @Param("status") String status,
            Pageable pageable
    );

    /**
     * Check if passport PIN exists
     */
    boolean existsByPassportPin(String passportPin);

    /**
     * Check if student ID number exists
     */
    boolean existsByStudentIdNumber(String studentIdNumber);

    // NO DELETE METHODS (NDG - Non-Deletion Guarantee)
}
